package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.service.oauth.LastfmService
import models.util.TextWrangler
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LastfmController extends Controller {

  def login = IdentifiedBySession { implicit request =>
    val state = TextWrangler.generateRandomString(16)
    val withState = LastfmService.queryString + ("state" -> Seq(state))
    Redirect(LastfmService.apiEndpoints.authorize, withState)
      .withCookies(Cookie(LastfmService.cookieKey, state))
  }

  def callback = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    val state = request.getQueryString("state")
    val code = request.getQueryString("token").orNull
    val cookieState = request.cookies.get(LastfmService.cookieKey)
    /*if(TextWrangler.validateState(cookieState, state)) {
      val futureJson = DeezerService.requestUserData(code)
      futureJson map {
        case Some(json) => Ok(json)
        case None => Ok("An error has occurred.")
      }
    }
    else Future.successful(Ok(Constants.stateMismatchError))*/
    LastfmService(identifier).requestUserData(code)
    Future.successful(Redirect(routes.CollectionController.index("lastfm")))
  }
}