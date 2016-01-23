package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.database.facade.TrackFacade
import models.database.facade.service.NapsterArtistFacade
import models.service.Constants
import models.service.api.{ApiFacade, NapsterApiFacade}
import models.service.oauth.{NapsterService, OauthRouting}
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NapsterController extends StreamingServiceController with AnalysisController with AlbumInfoController {

  override val redirectionService: OauthRouting = NapsterService
  override val apiFacade: ApiFacade = NapsterApiFacade
  override val serviceName: String = Constants.serviceNapster

  override def requestUserData(code: String, identifier: Either[Int, String]): Unit = {
    NapsterService(identifier).requestUserData(code)
  }

  override def artistsAndAlbumsForOverview(identifier: Either[Int, String]): Future[JsValue] = {
    NapsterArtistFacade(identifier).getArtistsAndAlbumsForOverview
  }
}