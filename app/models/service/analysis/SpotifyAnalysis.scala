package models.service.analysis

import models.database.facade.{ArtistFacade, AlbumFacade}
import models.service.api.SpotifyApiFacade
import models.util.Logging
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.WS
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SpotifyAnalysis(identifier:Either[Int,String]) extends ServiceAnalysis(identifier, "spotify") {

  val token:Option[String] = serviceAccessTokenCache.getAccessToken

  val searchEndpoint = "https://api.spotify.com/v1/artists/"

  val albumFacade = AlbumFacade(identifier)
  val artistFacade = ArtistFacade(identifier)

  def analyse():Future[JsValue] = {
    for {
      albums <- albumFacade.getUsersAlbumCollection
      ids <- getIds(albums)
      albums <- getAlbumsOfArtists(ids)
      res = processResponses(albums)
    } yield albums.head._2
  }

  private def getIds(albums: Option[Map[String, Set[String]]]):Future[Set[Option[(String,String)]]] = {
    albums match {
      case Some(albs) =>
        val artists = albs.keySet
        Future.sequence {
          artists.map { artist =>
            artistFacade.getArtistByName(artist) flatMap {
              case Some(art) =>
                art.spotifyId match {
                  case Some(id) => Future.successful(Some((artist, id)))
                  case None => SpotifyApiFacade.getArtistId(artist)
                }
              case None => SpotifyApiFacade.getArtistId(artist)
            }
          }
        }
      case None => Future.successful(Set())
    }
  }

  private def getAlbumsOfArtists(ids: Set[Option[(String,String)]]):Future[Set[(String,JsValue)]] = {
    val searchList:Set[(String,String)] = ids.filter(_.isDefined).map(_.get)
    Future.sequence {
      searchList map { artist =>
        val artistName = artist._1
        val artistId = artist._2
        val url = searchEndpoint + artistId + "/albums?market=DE&album_type=album,single&limit=50"
        token match {
          case Some(t) =>
            WS.url(url).withHeaders("Authorization" -> s"Bearer $t").get() map { response =>
              if(response.status != 200) {
                Logging.debug(this.getClass.toString, response.body.toString)
              }
              (artistName, Json.parse(response.body))
            }
          case None => Future.failed(new Exception("An access token could not be found"))
        }
      }
    }
  }

  private def processResponses(jsSet: Set[(String,JsValue)]):Map[String,Set[String]] = {
    jsSet.toList.foldLeft(Map[String,Set[String]]()) { (prev, jsTuple) =>
      prev ++ processSingleResponse(jsTuple)
    }
  }

  private def processSingleResponse(artistJsTuple:(String,JsValue)):Map[String,Set[String]] = {
    val artist = artistJsTuple._1
    val js = artistJsTuple._2
    val items = (js \ "items").as[List[JsValue]]
    val albums:Set[String] = items.map(item => (item \ "name").as[String]).toSet
    Map(artist -> albums)
  }
}

object SpotifyAnalysis {
  def apply(identifier:Either[Int,String]) = new SpotifyAnalysis(identifier)
}
