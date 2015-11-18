package controllers

import models.auth.IdentifiedBySession
import models.service.oauth.SoundcloudService
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Controller

class SoundcloudController extends Controller {

  def login = IdentifiedBySession { implicit request =>
    Redirect("https://api.soundcloud.com/connect", SoundcloudService.queryString)
  }

  def callback = IdentifiedBySession.async { implicit request =>
    val code = request.getQueryString("code").orNull
    //TODO check state to protect from CSRF
    for {
      response <- SoundcloudService.requestUserData(code)
    } yield Ok(response)
  }
}