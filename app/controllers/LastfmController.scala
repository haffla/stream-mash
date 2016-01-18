package controllers

import models.service.oauth.{LastfmService, OauthRouting}

class LastfmController extends StreamingServiceController {

  override val redirectionService: OauthRouting = LastfmService
  override val serviceName: String = "lastfm"
  override def requestUserData(code: String, identifier: Either[Int, String]): Unit = false
}