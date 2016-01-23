package controllers

import models.service.Constants
import models.service.oauth.{LastfmService, OauthRouting}

class LastfmController extends StreamingServiceController {

  override val redirectionService: OauthRouting = LastfmService
  override val serviceName: String = Constants.serviceLastFm
  override val serviceSupportsCSRFProtection = false

  override def requestUserData(code: String, identifier: Either[Int, String]): Unit = {
    LastfmService(identifier).requestUserData(code)
  }
}