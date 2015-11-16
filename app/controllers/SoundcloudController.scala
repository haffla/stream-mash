package controllers

import models.service.oauth.SoundcloudService
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

class SoundcloudController extends Controller {

  def login = Action { implicit request =>
    Redirect("https://api.soundcloud.com/connect", SoundcloudService.queryString)
  }

  def callback = Action.async { implicit request =>
    val code = request.getQueryString("code").orNull
    //TODO check state to protect from CSRF
    for {
      response <- SoundcloudService.requestUserData(code)
    } yield Ok(response)
  }
}