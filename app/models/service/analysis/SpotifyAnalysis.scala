package models.service.analysis

import models.database.alias.Artist
import models.database.facade.service.{SpotifyAlbumFacade, SpotifyArtistFacade}
import models.service.api.SpotifyApiFacade
import models.service.api.refresh.SpotifyRefresh
import models.service.oauth.SpotifyService
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSRequest, WS}
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SpotifyAnalysis(identifier:Either[Int,String],
                      usersFavouriteArtists: List[Artist])
                      extends ServiceAnalysis(identifier, usersFavouriteArtists, "spotify") {

  override val searchEndpoint = SpotifyService.apiEndpoints.artists
  override val serviceArtistFacade = SpotifyArtistFacade
  override val serviceAlbumFacade = SpotifyAlbumFacade
  override val apiFacade = SpotifyApiFacade
  val market = "DE"
  val limit = "50"
  val album_types = "album,single"

  def urlForRequest(artistId:String):String = searchEndpoint + "/" + artistId +
                                              s"/albums?market=$market&limit=$limit&album_type=$album_types"

  override def handleJsonResponse(jsResp:JsValue):List[(String,String)] = {
    val items = (jsResp \ "items").as[List[JsValue]]
    items.map {
      item =>
        val albumName = (item \ "name").as[String]
        val id = (item \ "id").as[String]
        (albumName, id)
    }
  }

  override def getServiceFieldFromArtist(artist: Artist): Option[String] = artist.spotifyId

  override def getAuthenticatedRequest(url:String, accessToken:String):WSRequest = {
    WS.url(url).withHeaders("Authorization" -> s"Bearer $accessToken")
  }

  /**
    * Before we get started, test the access token with some random request
    * If we get a 401 we need to refresh the token
    */
  override def testAndGetAccessToken():Future[Option[String]] = {
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

object SpotifyAnalysis {
  def apply(identifier:Either[Int,String], userFavouriteArtists: List[Artist])
                = new SpotifyAnalysis(identifier, userFavouriteArtists)
}
