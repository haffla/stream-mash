package controllers

import models.auth.{IdentifiedBySession, Helper}
import models.service.Constants
import models.service.oauth.RdioService
import models.util.TextWrangler
import play.api.mvc.{Cookie, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RdioController extends Controller {

  def login = IdentifiedBySession { implicit request =>
    val state = TextWrangler.generateRandomString(16)
    val withState = RdioService.queryString + ("state" -> Seq(state))
    Redirect(RdioService.apiEndpoints.authorize, withState)
      .withCookies(Cookie(RdioService.cookieKey, state))
  }

  def callback = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    val state = request.getQueryString("state")
    val cookieState = request.cookies.get(RdioService.cookieKey)
    val code = request.getQueryString("code").orNull
    if(TextWrangler.validateState(cookieState, state)) {
      RdioService(identifier).requestUserData(code)
      Future.successful(Redirect(routes.ItunesController.index("rdio")))
    }
    else Future.successful(Ok(Constants.stateMismatchError))
  }
}