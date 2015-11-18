package controllers

import models.auth.{IdentifiedBySession, Helper}
import models.database.facade.SpotifyFacade
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
    val code = request.getQueryString("code").orNull
    val storedState = request.cookies.get(SpotifyService.cookieKey) match {
      case Some(cookie) => cookie.value
      case None => Future.successful(
        Redirect(routes.Application.index).flashing("message" -> "There has been a problem...")
        )
    }
    state match {
      case Some(s) =>
        if(s == storedState) {
          SpotifyService(identifier).requestUserData(code) map {
            case Some(js) => Redirect(routes.ItunesController.index())
            case None => Ok("An error has occurred.")
          }
        }
        else Future.successful(Ok(Constants.stateMismatchError))
      case None => Future.successful(Ok(Constants.stateMismatchError))
    }
  }

  def getSpotifyArtistId = IdentifiedBySession.async { implicit request =>
    val artist = request.getQueryString("artist").get
    SpotifyFacade.getSpotifyIdForArtistFromDb(artist) flatMap {
      case Some(spotifyId) => Future.successful(Ok(Json.toJson(Map("spotify_id" -> spotifyId))))
      case None =>
        val id:Future[Option[String]] = SpotifyApiFacade.getArtistId(artist)
        id map {
          case Some(sp) => Ok(Json.toJson(Map("spotify_id" -> sp)))
          case None => Ok(Json.toJson(Map("error" -> "Did not find a Spotify ID")))
        }
    }
  }
}