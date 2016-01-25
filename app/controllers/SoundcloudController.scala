package controllers

import models.service.oauth.{OauthRouting, SoundcloudService}
import models.util.Constants

class SoundcloudController extends StreamingServiceController {

  override val redirectionService: OauthRouting = SoundcloudService
  override val serviceName: String = Constants.serviceSoundcloud

  override def requestUserData(code: String, identifier: Either[Int, String]): Unit = {
    SoundcloudService(identifier).requestUserData(code)
  }
}