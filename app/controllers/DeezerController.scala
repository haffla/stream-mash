package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.database.facade.{TrackFacade, DeezerArtistFacade}
import models.service.Constants
import models.service.api.{SpotifyApiFacade, DeezerApiFacade}
import models.service.oauth.DeezerService
import models.util.TextWrangler
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

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

  def getArtistsForAnalysis = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    DeezerArtistFacade(identifier).getArtistsAndAlbumsForOverview.map { jsonResult =>
      Ok(Json.obj("artists" -> jsonResult))
    }
  }

  def getArtistDetail = IdentifiedBySession.async { implicit request =>
    request.getQueryString("id").map { id =>
      DeezerApiFacade.getArtistInfoForFrontend(id).map { deezerResponse =>
        Ok(deezerResponse)
      }
    }.getOrElse(
      Future.successful(BadRequest("Missing parameter 'id', e.g. Deezer ID of the artist"))
    )
  }

  def getAlbumDetail = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    request.getQueryString("id").map { id =>
      val usersTracks = TrackFacade(identifier).getUsersTracks
      DeezerApiFacade.getAlbumInfoForFrontend(id,usersTracks).map { deezer =>
        Ok(deezer)
      }
    }.getOrElse(
      Future.successful(BadRequest("Missing parameter 'id', e.g. Spotify ID of the album"))
    )
  }
}