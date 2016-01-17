package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.service.Constants
import models.service.oauth.LastfmService
import models.util.TextWrangler
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LastfmController extends Controller {

  def login = IdentifiedBySession { implicit request =>
    Redirect(LastfmService.apiEndpoints.authorize, LastfmService.queryString)
  }

  def callback = IdentifiedBySession { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    request.getQueryString("token") match {
      case Some(c) =>
        LastfmService(identifier).requestUserData(c)
       Redirect(routes.CollectionController.index("lastfm"))
      case _ =>
        Redirect(routes.CollectionController.index())
          .flashing("message" -> Constants.missingOAuthCodeError)
    }

  }
}