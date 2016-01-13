package models.service.analysis

import models.database.facade.{SpotifyAlbumFacade, SpotifyArtistFacade, ArtistFacade, AlbumFacade}
import models.service.api.SpotifyApiFacade
import models.service.api.refresh.SpotifyRefresh
import models.util.Logging
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.{WSResponse, WS}
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SpotifyAnalysis(identifier:Either[Int,String]) extends ServiceAnalysis(identifier, "spotify") {

  val token:Option[String] = serviceAccessTokenHelper.getAccessToken
  var refreshToken:Option[String] = None

  override val searchEndpoint = "https://api.spotify.com/v1/artists/"
  override val albumFacade = AlbumFacade(identifier)
  override val artistFacade = ArtistFacade(identifier)
  val serviceArtistFacade = SpotifyArtistFacade
  val serviceAlbumFacade = SpotifyAlbumFacade

  def analyse() = {
    val artists = artistFacade.getUsersArtists
    for {
      ids <- getIds(artists)
      albums <- getAlbumsOfArtists(ids)
      res = processResponses(albums)
    } yield albums.head._2
  }

  private def getIds(artists: List[models.database.alias.Artist]):Future[List[Option[(String,String)]]] = {
    if(artists.nonEmpty) {
      Future.sequence {
        artists.map { artist =>
          artist.spotifyId match {
            case Some(spoId) => Future.successful(Some(artist.name, spoId))
            case None => SpotifyApiFacade.getArtistId(artist.name)
          }
        }
      }
    }
    else Future.successful(Nil)
  }

  private def getAlbumsOfArtists(ids: List[Option[(String,String)]]):Future[List[(String,JsValue)]] = {
    val searchList:List[(String,String)] = ids.filter(_.isDefined).map(_.get)
    Future.sequence {
      searchList map { artist =>
        val artistName = artist._1
        val artistId = artist._2
        val url = searchEndpoint + artistId + "/albums?market=DE&album_type=album,single&limit=50"
        (token, refreshToken) match {
          case (Some(t), None) =>
            doRequest(url,t) flatMap { response =>
              if(response.status != 200) {
                Logging.debug(this.getClass.toString, response.body.toString)
              }
              if(response.status == 401) {
                SpotifyRefresh(identifier).refreshToken(t) flatMap { refreshTkn =>
                  refreshToken = Some(refreshTkn)
                  doRequest(url, refreshTkn) map  { resp =>
                    (artistName, Json.parse(response.body))
                  }
                }
              }
              else Future.successful(artistName, Json.parse(response.body))
            }
          case (_, Some(rTkn)) =>
            doRequest(url,rTkn) map { response =>
              if(response.status != 200) {
                Logging.debug(this.getClass.toString, response.body.toString)
              }
              (artistName, Json.parse(response.body))
            }
          case _ => Future.failed(new Exception("An access token could not be found"))
        }
      }
    }
  }

  def save(artistAlbumMap: Map[String,List[(String,String)]]) = {
    artistAlbumMap.foreach { entity =>
      val artist = entity._1
      val albums = entity._2
      val dbArtistId:Long = serviceArtistFacade.saveArtistWithName(artist)
      albums.foreach { alb =>
        val albumName = alb._1
        val id = alb._2
        serviceAlbumFacade.saveAlbumWithNameAndId(albumName, dbArtistId, id)
      }
    }
  }

  private def processResponses(jsSet: List[(String,JsValue)]):Map[String,List[(String,String)]] = {
    val res = jsSet.foldLeft(Map[String,List[(String,String)]]()) { (prev, jsTuple) =>
      prev ++ processSingleResponse(jsTuple)
    }
    save(res)
    res
  }

  private def processSingleResponse(artistJsTuple:(String,JsValue)):Map[String,List[(String,String)]] = {
    val artist = artistJsTuple._1
    val js = artistJsTuple._2
    val items = (js \ "items").as[List[JsValue]]
    val albums:List[(String,String)] = items.map{
      item =>
        val albumName = (item \ "name").as[String]
        val id = (item \ "id").as[String]
        (albumName, id)
    }
    Map(artist -> albums)
  }

  private def doRequest(url:String, token:String):Future[WSResponse] = {
    WS.url(url).withHeaders("Authorization" -> s"Bearer $token").get()
  }
}

object SpotifyAnalysis {
  def apply(identifier:Either[Int,String]) = new SpotifyAnalysis(identifier)
}
