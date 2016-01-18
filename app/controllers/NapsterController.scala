package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.database.facade.TrackFacade
import models.database.facade.service.{NapsterArtistFacade, SpotifyArtistFacade}
import models.service.Constants
import models.service.api.{NapsterApiFacade, SpotifyApiFacade}
import models.service.oauth.NapsterService
import models.util.TextWrangler
import play.api.libs.json.Json
import play.api.mvc.{Controller, Cookie}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

  def getArtistsForAnalysis = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    NapsterArtistFacade(identifier).getArtistsAndAlbumsForOverview.map { jsonResult =>
      Ok(Json.obj("artists" -> jsonResult))
    }
  }

  def getAlbumDetail = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    request.getQueryString("id").map { spId =>
      val usersTracks = TrackFacade(identifier).getUsersTracks
      NapsterApiFacade.getAlbumInfoForFrontend(spId,usersTracks).map { napsterResponse =>
        Ok(napsterResponse)
      }
    }.getOrElse(
      Future.successful(BadRequest("Missing parameter 'id', e.g. Spotify ID of the album"))
    )
  }
}