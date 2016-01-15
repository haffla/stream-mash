package controllers

import models.auth.{IdentifiedBySession, Helper}
import models.database.facade.{TrackFacade, SpotifyArtistFacade, ArtistFacade}
import models.service.Constants
import models.service.api.SpotifyApiFacade
import models.service.oauth.SpotifyService
import models.util.TextWrangler
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.{Cookie, Controller}

import scala.concurrent.Future

class SpotifyController extends Controller {

  def login = IdentifiedBySession { implicit request =>
    val state = TextWrangler.generateRandomString(16)
    val withState = SpotifyService.queryString + ("state" -> Seq(state))
    Redirect(SpotifyService.apiEndpoints.authorize, withState)
      .withCookies(Cookie(SpotifyService.cookieKey, state))
  }

  def callback = IdentifiedBySession { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    val state = request.getQueryString("state")
    val stateCookie = request.cookies.get(SpotifyService.cookieKey)
    if(TextWrangler.validateState(stateCookie, state)) {
      request.getQueryString("code") match {
        case Some(code) =>
          SpotifyService(identifier).requestUserData(code)
          Redirect(routes.CollectionController.index("spotify"))
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
    SpotifyArtistFacade(identifier).getArtistsAndAlbumsForOverview.map { jsonResult =>
      Ok(Json.obj("artists" -> jsonResult))
    }
  }

  def getArtistDetail = IdentifiedBySession.async { implicit request =>
    request.getQueryString("spId").map { spId =>
      SpotifyApiFacade.getArtistInfoForFrontend(spId).map { spotifyResponse =>
        Ok(spotifyResponse)
      }
    }.getOrElse(
      Future.successful(BadRequest("Missing parameter 'spId', e.g. Spotify ID of the artist"))
    )
  }

  def getAlbumDetail = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    request.getQueryString("spId").map { spId =>
      val usersTracks = TrackFacade(identifier).getUsersTracks
      SpotifyApiFacade.getAlbumInfoForFrontend(spId,usersTracks).map { spotifyResponse =>
        Ok(spotifyResponse)
      }
    }.getOrElse(
      Future.successful(BadRequest("Missing parameter 'spId', e.g. Spotify ID of the album"))
    )
  }

  def getSpotifyArtistId = IdentifiedBySession.async { implicit request =>
    request.getQueryString("artist").map { artist =>
      val mayBeId = ArtistFacade.getArtistByName(artist) match {
        case Some(a) => a.spotifyId
        case None => None
      }
      mayBeId match {
        case Some(spoId) => Future.successful(Ok(Json.toJson(Map("spotify_id" -> spoId))))
        case None => SpotifyApiFacade.getArtistId(artist).map {
          case Some(spId) =>
            val id = spId._2
            Ok(Json.toJson(Map("spotify_id" -> id)))
          case None => Ok(Json.toJson(Map("error" -> "Did not find a Spotify ID")))
        }
      }
    }.getOrElse(Future.successful(Ok(Json.toJson(Map("error" -> "No artist specified")))))
  }
}