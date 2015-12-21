package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.service.Constants
import models.service.oauth.DeezerService
import models.util.TextWrangler
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeezerController extends Controller {

  def login = IdentifiedBySession { implicit request =>
    val state = TextWrangler.generateRandomString(16)
    val withState = DeezerService.queryString + ("state" -> Seq(state))
    Redirect(DeezerService.apiEndpoints.authorize, withState)
      .withCookies(Cookie(DeezerService.cookieKey, state))
  }

  def callback = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    val state = request.getQueryString("state")
    val code = request.getQueryString("code").orNull
    val cookieState = request.cookies.get(DeezerService.cookieKey)
    if(TextWrangler.validateState(cookieState, state)) {
      DeezerService(identifier).requestUserData(code)
      Future.successful(Redirect(routes.ItunesController.index("deezer")))
    }
    else Future.successful(Ok(Constants.stateMismatchError))
  }
}