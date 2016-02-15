package controllers

import models.database.facade.service.SpotifyArtistFacade
import models.service.api.{ApiFacade, SpotifyApiFacade}
import models.service.oauth.{OAuthRouting, SpotifyService}
import models.util.Constants
import play.api.libs.json.JsValue

import scala.concurrent.Future

class SpotifyController extends StreamingServiceController with AnalysisController with AlbumInfoController {

  override val redirectionService: OAuthRouting = SpotifyService
  override val serviceName: String = Constants.serviceSpotify
  override val apiFacade: ApiFacade = SpotifyApiFacade
  override def serviceClass(identifier:Either[Int,String]) = SpotifyService(identifier)
  override def artistsAndAlbumsForOverview(identifier: Either[Int, String]): Future[JsValue] = {
    SpotifyArtistFacade(identifier).getArtistsAndAlbumsForOverview
  }
}