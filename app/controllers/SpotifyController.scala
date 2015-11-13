package controllers

import database.facade.artistFacade
import models.auth.Authenticated
import models.service.{Constants, SpotifyService}
import models.util.TextWrangler
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class SpotifyController extends Controller {

  def login = Action { implicit request =>
    val state = TextWrangler.generateRandomString(16)
    val withState = SpotifyService.queryString + ("state" -> Seq(state))
    Redirect(SpotifyService.apiEndpoints.authorize, withState)
      .withCookies(Cookie(SpotifyService.cookieKey, state))
  }

  def callback = Action.async { implicit request =>
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
          val json = SpotifyService.requestUserData(code)
          json map {
            case Some(js) => Ok(js)
            case None => Ok("An error has occurred.")
          }
        }
        else Future.successful(Ok(Constants.stateMismatchError))
      case None => Future.successful(Ok(Constants.stateMismatchError))
    }
  }

  def getSpotifyArtistId = Authenticated.async { implicit request =>
    val artist = request.getQueryString("artist").get
    artistFacade.getSpotifyIdForArtistFromDb(artist) flatMap {
      case Some(spotifyId) => Future.successful(Ok(Json.toJson(Map("spotify_id" -> spotifyId))))
      case None =>
        val id:Future[Option[String]] = artistFacade.getSpotifyIdForArtistFromSpotify(artist)
        id map {
          case Some(sp) => Ok(Json.toJson(Map("spotify_id" -> sp)))
          case None => Ok(Json.toJson(Map("error" -> "Did not find a Spotify ID")))
        }
    }
  }
}