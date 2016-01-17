package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.service.Constants
import models.service.oauth.NapsterService
import models.util.TextWrangler
import play.api.mvc.{Controller, Cookie}

class NapsterController extends Controller {

  def login = IdentifiedBySession { implicit request =>
    val state = TextWrangler.generateRandomString(16)
    val withState = NapsterService.queryString + ("state" -> Seq(state))
    Redirect(NapsterService.apiEndpoints.authorize, withState)
      .withCookies(Cookie(NapsterService.cookieKey, state))
  }

  def callback = IdentifiedBySession { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    val state = request.getQueryString("state")
    val stateCookie = request.cookies.get(NapsterService.cookieKey)
    if(TextWrangler.validateState(stateCookie, state)) {
      request.getQueryString("code") match {
        case Some(code) =>
          NapsterService(identifier).requestUserData(code)
          Redirect(routes.CollectionController.index("napster"))
        case None =>
          Ok(Constants.missingOAuthCodeError)
      }
    }
    else {
      Ok(Constants.stateMismatchError)
    }
  }
}