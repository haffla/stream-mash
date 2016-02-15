package controllers

import models.database.facade.service.NapsterArtistFacade
import models.service.api.{ApiFacade, NapsterApiFacade}
import models.service.oauth.{NapsterService, OAuthRouting}
import models.util.Constants
import play.api.libs.json.JsValue

import scala.concurrent.Future

class NapsterController extends StreamingServiceController with AnalysisController with AlbumInfoController {

  override val redirectionService: OAuthRouting = NapsterService
  override val apiFacade: ApiFacade = NapsterApiFacade
  override val serviceName: String = Constants.serviceNapster
  override def serviceClass(identifier:Either[Int,String]) = NapsterService(identifier)
  override def artistsAndAlbumsForOverview(identifier: Either[Int, String]): Future[JsValue] = {
    NapsterArtistFacade(identifier).getArtistsAndAlbumsForOverview
  }
}