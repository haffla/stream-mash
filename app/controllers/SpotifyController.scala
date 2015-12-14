package controllers

import models.auth.{IdentifiedBySession, Helper}
import models.database.facade.ArtistFacade
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

  def callback = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    val state = request.getQueryString("state")
    val stateCookie = request.cookies.get(SpotifyService.cookieKey)
    if(TextWrangler.validateState(stateCookie, state)) {
      request.getQueryString("code") match {
        case Some(code) =>
          SpotifyService(identifier).requestUserData(code)
          Future.successful(Redirect(routes.ItunesController.index("spotify")))
        case None =>
          Future.successful(Ok(Constants.missingOAuthCodeError))
      }
    }
    else {
      Future.successful(Ok(Constants.stateMismatchError))
    }
  }

  def getSpotifyArtistId = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    val artistName = request.getQueryString("artist").get
    ArtistFacade(identifier).getArtistByName(artistName) map {
      case Some(artist) => artist.spotifyId
      case None => None
    } flatMap {
      case Some(id) => Future.successful(Ok(Json.toJson(Map("spotify_id" -> id))))
      case None =>
        val id:Future[Option[String]] = SpotifyApiFacade.getArtistId(artistName)
        id map {
          case Some(spId) => Ok(Json.toJson(Map("spotify_id" -> spId)))
          case None => Ok(Json.toJson(Map("error" -> "Did not find a Spotify ID")))
        }
    }

  }
}