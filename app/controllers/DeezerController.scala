package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.database.facade.TrackFacade
import models.database.facade.service.DeezerArtistFacade
import models.service.api.{ApiFacade, DeezerApiFacade}
import models.service.oauth.{DeezerService, OauthRouting}
import models.util.Constants
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeezerController extends StreamingServiceController with AnalysisController with AlbumInfoController {

  override val redirectionService: OauthRouting = DeezerService
  override val serviceName: String = Constants.serviceDeezer
  override val apiFacade: ApiFacade = DeezerApiFacade

  override def requestUserData(code: String, identifier: Either[Int, String]): Unit = {
    DeezerService(identifier).requestUserData(code)
  }

  override def artistsAndAlbumsForOverview(identifier: Either[Int, String]): Future[JsValue] = {
    DeezerArtistFacade(identifier).getArtistsAndAlbumsForOverview
  }
}