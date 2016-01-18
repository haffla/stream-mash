package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.database.facade.TrackFacade
import models.database.facade.service.DeezerArtistFacade
import models.service.api.DeezerApiFacade
import models.service.oauth.{DeezerService, OauthRouting}
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeezerController extends StreamingServiceController with AnalysisController {

  override val redirectionService: OauthRouting = DeezerService
  override val serviceName: String = "deezer"

  override def requestUserData(code: String, identifier: Either[Int, String]): Unit = {
    DeezerService(identifier).requestUserData(code)
  }

  override def artistsAndAlbumsForOverview(identifier: Either[Int, String]): Future[JsValue] = {
    DeezerArtistFacade(identifier).getArtistsAndAlbumsForOverview
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