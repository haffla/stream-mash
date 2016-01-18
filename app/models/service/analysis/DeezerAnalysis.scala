package models.service.analysis

import models.database.alias.Artist
import models.database.facade.service.{DeezerAlbumFacade, DeezerArtistFacade}
import models.service.api.DeezerApiFacade
import models.service.oauth.DeezerService
import models.util.Logging
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.libs.ws.{WS, WSRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeezerAnalysis(identifier:Either[Int,String],
                     userFavouriteArtists: List[Artist])
                     extends ServiceAnalysis(identifier, userFavouriteArtists, "deezer") {

  override val searchEndpoint = DeezerService.apiEndpoints.artists
  override val serviceArtistFacade = DeezerArtistFacade
  override val serviceAlbumFacade = DeezerAlbumFacade
  override val apiFacade = DeezerApiFacade

  def urlForRequest(artistId:String):String = searchEndpoint + "/" + artistId + "/albums?output=json"

  override def handleJsonResponse(jsResp:JsValue):List[(String,String)] = {
    val items = (jsResp \ "data").as[List[JsValue]]
    items.map {
      item =>
        val albumName = (item \ "title").as[String]
        val id = (item \ "id").as[Int]
        (albumName, id.toString)
    }
  }

  override def getServiceFieldFromArtist(artist: Artist): Option[String] = artist.deezerId

  override def getAuthenticatedRequest(url:String, accessToken:String):WSRequest = {
    WS.url(url).withQueryString("access_token" -> accessToken)
  }

  /**
    * Before we get started, test the access token with some random request
    * If we get a 401 we need to refresh the token
    */
  override def testAndGetAccessToken():Future[Option[String]] = {
    serviceAccessTokenHelper.getAccessToken match {
      case Some(accessTkn) =>
        val url = urlForRequest("27")
        getAuthenticatedRequest(url, accessTkn).get().flatMap {
          response =>
            if(response.status != 200) {
              Logging.debug(ich, "Testing the access token was not successful")
              Future.successful(None)
            }
            else {
              Future.successful(Some(accessTkn))
            }
        }
      case None =>
        serviceAccessTokenHelper.getAnyAccessToken match {
          case Some(tkn) =>
            serviceAccessTokenHelper.setAccessToken(tkn)
            testAndGetAccessToken()
          case None => Future.successful(None)
        }
    }
  }
}

object DeezerAnalysis {
  def apply(identifier:Either[Int,String], userFavouriteArtists: List[Artist])
              = new DeezerAnalysis(identifier, userFavouriteArtists)
}


