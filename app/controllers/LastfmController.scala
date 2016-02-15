package controllers

import models.service.oauth.{LastfmService, OAuthRouting}
import models.util.Constants

class LastfmController extends StreamingServiceController {

  override val redirectionService: OAuthRouting = LastfmService
  override val serviceName: String = Constants.serviceLastFm
  override val serviceSupportsCSRFProtection = false
  override val keyCode = "token"
  override def serviceClass(identifier:Either[Int,String]) = LastfmService(identifier)
}