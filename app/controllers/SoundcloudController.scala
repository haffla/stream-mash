package controllers

import models.service.oauth.{OauthRouting, SoundcloudService}

class SoundcloudController extends StreamingServiceController {

  override val redirectionService: OauthRouting = SoundcloudService
  override val serviceName: String = "soundcloud"

  override def requestUserData(code: String, identifier: Either[Int, String]): Unit = {
    SoundcloudService(identifier).requestUserData(code)
  }
}