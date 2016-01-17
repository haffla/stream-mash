package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.service.Constants
import models.service.oauth.DeezerService
import models.util.TextWrangler
import play.api.mvc._

class DeezerController extends Controller {

  def login = IdentifiedBySession { implicit request =>
    val state = TextWrangler.generateRandomString(16)
    val withState = DeezerService.queryString + ("state" -> Seq(state))
    Redirect(DeezerService.apiEndpoints.authorize, withState)
      .withCookies(Cookie(DeezerService.cookieKey, state))
  }

  def callback = IdentifiedBySession { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    val state = request.getQueryString("state")
    request.getQueryString("code") match {
      case Some(c) =>
        val cookieState = request.cookies.get(DeezerService.cookieKey)
        if(TextWrangler.validateState(cookieState, state)) {
          DeezerService(identifier).requestUserData(c)
          Redirect(routes.CollectionController.index("deezer"))
        }
        else Ok(Constants.stateMismatchError)
      case _ =>
        Redirect(routes.CollectionController.index())
          .flashing("message" -> Constants.missingOAuthCodeError)
    }

  }
}