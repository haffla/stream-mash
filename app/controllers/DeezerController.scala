package controllers

import models.service.Constants
import models.service.oauth.DeezerService
import models.util.TextWrangler
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeezerController extends Controller {

  def login = Action { implicit request =>
    val state = TextWrangler.generateRandomString(16)
    val withState = DeezerService.queryString + ("state" -> Seq(state))
    Redirect(DeezerService.apiEndpoints.authorize, withState)
      .withCookies(Cookie(DeezerService.cookieKey, state))
  }

  def callback = Action.async { implicit request =>
    val state = request.getQueryString("state")
    val code = request.getQueryString("code").orNull
    val storedState = request.cookies.get(DeezerService.cookieKey) match {
      case Some(cookie) => cookie.value
      case None => Future.successful(
        Redirect(routes.Application.index).flashing("message" -> "There has been a problem...")
      )
    }
    state match {
      case Some(s) =>
        if(s == storedState) {
          val futureJson = DeezerService.requestUserData(code)
          futureJson map {
            case Some(json) => Ok(json)
            case None => Ok("An error has occurred.")
          }
        }
        else Future.successful(Ok(Constants.stateMismatchError))
      case None => Future.successful(Ok(Constants.stateMismatchError))
    }
  }
}