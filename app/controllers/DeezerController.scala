package controllers

import models.database.facade.service.DeezerArtistFacade
import models.service.api.{ApiFacade, DeezerApiFacade}
import models.service.oauth.{DeezerService, OAuthRouting}
import models.util.Constants
import play.api.libs.json.JsValue

import scala.concurrent.Future

class DeezerController extends StreamingServiceController with AnalysisController with AlbumInfoController {

  override val redirectionService: OAuthRouting = DeezerService
  override val serviceName: String = Constants.serviceDeezer
  override val apiFacade: ApiFacade = DeezerApiFacade
  override def serviceClass(identifier:Either[Int,String]) = DeezerService(identifier)
  override def artistsAndAlbumsForOverview(identifier: Either[Int, String]): Future[JsValue] = {
    DeezerArtistFacade(identifier).getArtistsAndAlbumsForOverview
  }
}