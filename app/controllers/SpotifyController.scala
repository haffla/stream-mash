package controllers

import models.service.SpotifyService
import models.util.TextWrangler
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.ws._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class SpotifyController extends Controller {

  def loginPage = Action { implicit request =>
    val state = TextWrangler.generateRandomString(16)
    val withState = SpotifyService.queryString + ("state" -> Seq(state))
    Redirect(SpotifyService.ApiEndpoints.AUTHORIZE, withState)
      .withCookies(Cookie(SpotifyService.SPOTIFY_COOKIE_KEY, state))
  }



  def callback = Action.async { implicit request =>
    val state = request.getQueryString("state").orNull
    val code = request.getQueryString("code").orNull
    val storedState = request.cookies.get(SpotifyService.SPOTIFY_COOKIE_KEY) match {
      case Some(cookie) => cookie.value
      case None => Future.successful(
        Redirect(routes.AuthController.logout).flashing("message" -> "There has been a problem while")
        )
    }
    //CSRF Protection
    if(state == null || state != storedState) {
      Future.successful(Ok("Error: State Mismatch"))
    } else {
      val wsResponse: Future[Option[WSResponse]] = SpotifyService.requestAuthorization(code)
      wsResponse.map {
        case Some(response) => Ok(Json.parse(response.body).toString())
        case None => Ok("")
      }
    }

  }


}