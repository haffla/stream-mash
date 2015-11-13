package controllers

import models.service.{Constants, RdioService}
import models.util.TextWrangler
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RdioController extends Controller {

  def login = Action { implicit request =>
    val state = TextWrangler.generateRandomString(16)
    val withState = RdioService.queryString + ("state" -> Seq(state))
    Redirect(RdioService.apiEndpoints.authorize, withState)
      .withCookies(Cookie(RdioService.cookieKey, state))
  }

  def callback = Action.async { implicit request =>
    val userId:Int = request.session.get("user_id").get.toInt
    val state = request.getQueryString("state")
    val code = request.getQueryString("code").orNull
    val storedState = request.cookies.get(RdioService.cookieKey) match {
      case Some(cookie) => cookie.value
      case None => Future.successful(
        Redirect(routes.Application.index).flashing("message" -> "There has been a problem...")
      )
    }
    state match {
      case Some(s) =>
        if(s == storedState) {
          val futureJson = RdioService(userId).requestUserData(code)
          futureJson map { json =>
            Ok(json)
          }
        }
        else Future.successful(Ok(Constants.stateMismatchError))
      case None => Future.successful(Ok(Constants.stateMismatchError))
    }
  }
}