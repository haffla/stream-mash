package models.service.analysis

import models.database.facade.{SpotifyAlbumFacade, SpotifyArtistFacade, ArtistFacade, AlbumFacade}
import models.service.api.SpotifyApiFacade
import models.service.api.refresh.SpotifyRefresh
import models.service.oauth.SpotifyService
import models.util.Logging
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.WS
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SpotifyAnalysis(identifier:Either[Int,String]) extends ServiceAnalysis(identifier, "spotify") {

  override val searchEndpoint = SpotifyService.apiEndpoints.artists
  override val albumFacade = AlbumFacade(identifier)
  override val artistFacade = ArtistFacade(identifier)
  val serviceArtistFacade = SpotifyArtistFacade
  val serviceAlbumFacade = SpotifyAlbumFacade

  def analyse():Future[Boolean] = {
    val artists = artistFacade.getUsersArtists
    for {
      accessToken <- testAndGetAccessToken()
      ids <- getIds(artists, Some(accessToken))
      albums <- getAlbumsOfArtistsFromSpotify(ids, accessToken)
      res = processResponses(albums)
    } yield true
  }

  /**
    * Before we get started, test the access token with some random request
    * If we get a 401 we need to refresh the token
    */
  def testAndGetAccessToken():Future[String] = {
    serviceAccessTokenHelper.getAccessToken match {
      case Some(accessTkn) =>
        val url = searchEndpoint + "/0OdUWJ0sBjDrqHygGUXeCF"
        WS.url(url).withHeaders("Authorization" -> s"Bearer $accessTkn").get().flatMap {
          response =>
            if(response.status == 401)
              SpotifyRefresh(identifier).refreshToken()
            else Future.successful(accessTkn)
        }
      case None =>
        serviceAccessTokenHelper.getAnyAccessTokens match {
          case Some(tokens) =>
            serviceAccessTokenHelper.setAccessToken(tokens.accessToken, Some(tokens.refreshToken))
            testAndGetAccessToken()
          case None => Future.failed(new Exception("Cannot continue without access tokens."))
        }
    }
  }

  private def getIds(
          artists: List[models.database.alias.Artist],
          token:Option[String]):Future[List[Option[(String,String)]]] = {
    if(artists.nonEmpty) {
      Future.sequence {
        artists.map { artist =>
          artist.spotifyId match {
            case Some(spoId) => Future.successful(Some(artist.name, spoId))
            case None => SpotifyApiFacade.getArtistId(artist.name, token, Some(identifier))
          }
        }
      }
    }
    else Future.successful(Nil)
  }

  private def getAlbumsOfArtistsFromSpotify(
          ids: List[Option[(String,String)]],
          accessToken:String):Future[List[(String,List[JsValue])]] = {
    val searchList:List[(String,String)] = ids.filter(_.isDefined).map(_.get)
    Future.sequence {
      searchList map { artist =>
        val artistName = artist._1
        val artistId = artist._2
        val url = searchEndpoint + "/" + artistId + "/albums?market=DE&limit=50"
        doRequest(url,accessToken,Nil) map(jsonResponses => (artistName, jsonResponses))
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

  private def processResponses(jsSet: List[(String,List[JsValue])]):Map[String,List[(String,String)]] = {
    val res = jsSet.foldLeft(Map[String,List[(String,String)]]()) { (prev, jsTuple) =>
      prev ++ processSingleResponse(jsTuple)
    }
    save(res)
    res
  }

  private def processSingleResponse(artistJsTuple:(String,List[JsValue])):Map[String,List[(String,String)]] = {
    val artist = artistJsTuple._1
    val jsResponseList = artistJsTuple._2
    val albums:List[(String,String)] = jsResponseList.flatMap { jsResp =>
      val items = (jsResp \ "items").as[List[JsValue]]
      items.map {
        item =>
          val albumName = (item \ "name").as[String]
          val id = (item \ "id").as[String]
          (albumName, id)
      }
    }
    Map(artist -> albums)
  }

  private def doRequest(url:String, token:String, jsonResponses:List[JsValue]):Future[List[JsValue]] = {
    WS.url(url).withHeaders("Authorization" -> s"Bearer $token").get().flatMap { response =>
      if(response.status != 200) Logging.debug(this.getClass.toString, response.body.toString)
      val js = Json.parse(response.body)
      (js \ "next").asOpt[String] match {
        case Some(nextUrl) => doRequest(nextUrl, token, js::jsonResponses)
        case _ => Future.successful(js::jsonResponses)
      }
    }
  }
}

object SpotifyAnalysis {
  def apply(identifier:Either[Int,String]) = new SpotifyAnalysis(identifier)
}
