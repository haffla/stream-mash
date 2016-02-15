package controllers

import models.service.oauth.{OAuthRouting, SoundcloudService}
import models.util.Constants

class SoundcloudController extends StreamingServiceController {

  override val redirectionService: OAuthRouting = SoundcloudService
  override val serviceName: String = Constants.serviceSoundcloud
  override def serviceClass(identifier:Either[Int,String]) = SoundcloudService(identifier)
}