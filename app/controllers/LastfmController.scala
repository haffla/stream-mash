package controllers

import models.service.oauth.{LastfmService, OauthRouting}
import models.util.Constants

class LastfmController extends StreamingServiceController {

  override val redirectionService: OauthRouting = LastfmService
  override val serviceName: String = Constants.serviceLastFm
  override val serviceSupportsCSRFProtection = false
  override val keyCode = "token"

  override def requestUserData(code: String, identifier: Either[Int, String]): Unit = {
    LastfmService(identifier).requestUserData(code)
  }
}