package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.database.facade.TrackFacade
import models.database.facade.service.SpotifyArtistFacade
import models.service.api.SpotifyApiFacade
import models.service.oauth.{OauthRouting, SpotifyService}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue

import scala.concurrent.Future

class SpotifyController extends StreamingServiceController with AnalysisController {

  override val redirectionService: OauthRouting = SpotifyService
  override val serviceName: String = "spotify"

  override def requestUserData(code: String, identifier: Either[Int, String]): Unit = {
    SpotifyService(identifier).requestUserData(code)
  }

  override def artistsAndAlbumsForOverview(identifier: Either[Int, String]): Future[JsValue] = {
    SpotifyArtistFacade(identifier).getArtistsAndAlbumsForOverview
  }

  def getArtistDetail = IdentifiedBySession.async { implicit request =>
    request.getQueryString("id").map { spId =>
      SpotifyApiFacade.getArtistInfoForFrontend(spId).map { spotifyResponse =>
        Ok(spotifyResponse)
      }
    }.getOrElse(
      Future.successful(BadRequest("Missing parameter 'id', e.g. Spotify ID of the artist"))
    )
  }

  def getAlbumDetail = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    request.getQueryString("id").map { spId =>
      val usersTracks = TrackFacade(identifier).getUsersTracks
      SpotifyApiFacade.getAlbumInfoForFrontend(spId,usersTracks).map { spotifyResponse =>
        Ok(spotifyResponse)
      }
    }.getOrElse(
      Future.successful(BadRequest("Missing parameter 'id', e.g. Spotify ID of the album"))
    )
  }
}