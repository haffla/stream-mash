package controllers

import models.database.facade.service.{DeezerArtistFacade, ServiceArtistTrait}
import models.service.api.{ApiFacade, DeezerApiFacade}
import models.service.oauth.{DeezerService, OAuthRouting}
import models.util.Constants

class DeezerController extends StreamingServiceController with AnalysisController with AlbumInfoController {

  override val redirectionService: OAuthRouting = DeezerService
  override val serviceName: String = Constants.serviceDeezer
  override val serviceArtistFacade: ServiceArtistTrait = DeezerArtistFacade
  override val apiFacade: ApiFacade = DeezerApiFacade
  override def serviceClass(identifier:Either[Int,String]) = DeezerService(identifier)
}