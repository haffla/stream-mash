package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.database.facade.TrackFacade
import models.database.facade.service.SpotifyArtistFacade
import models.service.api.{ApiFacade, SpotifyApiFacade}
import models.service.oauth.{OauthRouting, SpotifyService}
import models.util.Constants
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue

import scala.concurrent.Future

class SpotifyController extends StreamingServiceController with AnalysisController with AlbumInfoController {

  override val redirectionService: OauthRouting = SpotifyService
  override val serviceName: String = Constants.serviceSpotify
  override val apiFacade: ApiFacade = SpotifyApiFacade

  override def requestUserData(code: String, identifier: Either[Int, String]): Unit = {
    SpotifyService(identifier).requestUserData(code)
  }

  override def artistsAndAlbumsForOverview(identifier: Either[Int, String]): Future[JsValue] = {
    SpotifyArtistFacade(identifier).getArtistsAndAlbumsForOverview
  }
}