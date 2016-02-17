package models.service.analysis

import models.database.alias.Artist
import models.database.facade.service.SpotifyArtistFacade
import models.service.api.SpotifyApiFacade
import models.service.api.refresh.SpotifyRefresh
import models.service.oauth.SpotifyService
import models.util.Constants
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.libs.ws.{WS, WSRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SpotifyAnalysis(identifier:Either[Int,String],
                      usersFavouriteArtists: List[Artist])
                      extends ServiceAnalysis(identifier, usersFavouriteArtists, Constants.serviceSpotify) {

  override val searchEndpoint = SpotifyService.apiEndpoints.artists
  override val serviceArtistFacade = SpotifyArtistFacade
  override val apiFacade = SpotifyApiFacade
  val market = "DE"
  val limit = "50"
  val albumTypes = "album,single"

  protected def urlForRequest(artistId:String):String = searchEndpoint + "/" + artistId +
                                              s"/albums?market=$market&limit=$limit&album_type=$albumTypes"

  protected override def handleJsonResponse(jsResp:JsValue):List[(String,String)] = {
    val items = (jsResp \ "items").as[List[JsValue]]
    items.map {
      item =>
        val albumName = (item \ "name").as[String]
        val id = (item \ "id").as[String]
        (albumName, id)
    }
  }

  protected override def getServiceFieldFromArtist(artist: Artist): Option[String] = artist.spotifyId

  protected override def getAuthenticatedRequest(url:String, accessToken:String):WSRequest = {
    WS.url(url).withHeaders("Authorization" -> s"Bearer $accessToken")
  }

  /**
    * Before we get started, test the access token with some random request
    * If we get a 401 we need to refresh the token
    */
  protected override def testAndGetAccessToken():Future[Option[String]] = {
    serviceAccessTokenHelper.getAccessToken match {
      case Some(accessTkn) =>
        val url = searchEndpoint + s"/0OdUWJ0sBjDrqHygGUXeCF/albums?market=$market&limit=1"
        getAuthenticatedRequest(url, accessTkn).get().flatMap {
          response =>
            if(response.status == 401)
              SpotifyRefresh(identifier).refreshToken()
            else {
              Future.successful(Some(accessTkn))
            }
        }
      case None =>
        serviceAccessTokenHelper.getAnyAccessTokenPair match {
          case Some(tokens) =>
            serviceAccessTokenHelper.setAccessToken(tokens.accessToken, Some(tokens.refreshToken))
            testAndGetAccessToken()
          case None => Future.successful(None)
        }
    }
  }
}

object SpotifyAnalysis extends ServiceAnalysisTrait {
  def apply(identifier:Either[Int,String], userFavouriteArtists: List[Artist])
                = new SpotifyAnalysis(identifier, userFavouriteArtists)
}
