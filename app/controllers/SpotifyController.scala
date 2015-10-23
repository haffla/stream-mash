package controllers

import models.service.SpotifyService
import models.util.TextWrangler
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.ws._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class SpotifyController extends Controller {

  def login = Action { implicit request =>
    val state = TextWrangler.generateRandomString(16)
    val withState = SpotifyService.queryString + ("state" -> Seq(state))
    Redirect(SpotifyService.apiEndpoints.authorize, withState)
      .withCookies(Cookie(SpotifyService.cookieKey, state))
  }

  def callback = Action.async { implicit request =>
    val state = request.getQueryString("state")
    val code = request.getQueryString("code").orNull
    val storedState = request.cookies.get(SpotifyService.cookieKey) match {
      case Some(cookie) => cookie.value
      case None => Future.successful(
        Redirect(routes.Application.index).flashing("message" -> "There has been a problem...")
        )
    }
    //CSRF Protection, see http://tools.ietf.org/html/rfc6749#section-10.12
    val stateMismatchMessage = "Error: State Mismatch"
    state match {
      case Some(s) =>
        if(s == storedState) {
          val wsResponse: Future[Option[WSResponse]] = SpotifyService.requestUserData(code)
          wsResponse.map {
            case Some(response) => Ok(Json.parse(response.body).toString())
            case None => Ok("An error has occurred.")
          }
        }
        else Future.successful(Ok(stateMismatchMessage))
      case None => Future.successful(Ok(stateMismatchMessage))
    }
  }
}