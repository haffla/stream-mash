package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.service.Constants
import models.service.oauth.SoundcloudService
import models.util.TextWrangler
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Cookie, Controller}

import scala.concurrent.Future

class SoundcloudController extends Controller {

  def login = IdentifiedBySession { implicit request =>
    val state = TextWrangler.generateRandomString(16)
    val withState = SoundcloudService.queryString + ("state" -> Seq(state))
    Redirect("https://api.soundcloud.com/connect", withState)
      .withCookies(Cookie(SoundcloudService.cookieKey, state))
  }

  def callback = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    val code = request.getQueryString("code").orNull
    val state = request.getQueryString("state")
    val stateCookie = request.cookies.get(SoundcloudService.cookieKey)
    if(TextWrangler.validateState(stateCookie, state)) {
      for {
        response <- SoundcloudService(identifier).requestUserData(code)
      } yield Redirect(routes.ItunesController.index())
    }
    else {
      Future.successful(Ok(Constants.stateMismatchError))
    }
  }
}