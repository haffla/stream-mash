package controllers

import models.service.SoundcloudService
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import org.haffla.soundcloud.Client

import scala.concurrent.Future

class SoundcloudController extends Controller {

  def login = Action { implicit request =>
    Redirect(SoundcloudService.ApiEndpoints.AUTHORIZE, SoundcloudService.queryString)
  }

  def callback = Action.async { implicit request =>
    val code = request.getQueryString("code").orNull
    //TODO check state to protect from CSRF
    val futureReponse = SoundcloudService.requestUserData(code)
    futureReponse.map {
      case Some(response) => Ok(Json.parse(response.body).toString())
      case None => Ok("An error has occurred...")
    }
  }
}