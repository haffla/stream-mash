package controllers

import models.database.facade.service.{NapsterArtistFacade, ServiceArtistTrait}
import models.service.api.{ApiFacade, NapsterApiFacade}
import models.service.oauth.{NapsterService, OAuthRouting}
import models.util.Constants

class NapsterController extends StreamingServiceController with AnalysisController with AlbumInfoController {

  override val redirectionService: OAuthRouting = NapsterService
  override val apiFacade: ApiFacade = NapsterApiFacade
  override val serviceArtistFacade: ServiceArtistTrait = NapsterArtistFacade
  override val serviceName: String = Constants.serviceNapster
  override def serviceClass(identifier:Either[Int,String]) = NapsterService(identifier)
}