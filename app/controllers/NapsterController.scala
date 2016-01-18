package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.database.facade.TrackFacade
import models.database.facade.service.NapsterArtistFacade
import models.service.api.NapsterApiFacade
import models.service.oauth.{NapsterService, OauthRouting}
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NapsterController extends StreamingServiceController with AnalysisController {

  override val redirectionService: OauthRouting = NapsterService

  override val serviceName: String = "napster"

  override def requestUserData(code: String, identifier: Either[Int, String]): Unit = {
    NapsterService(identifier).requestUserData(code)
  }

  override def artistsAndAlbumsForOverview(identifier: Either[Int, String]): Future[JsValue] = {
    NapsterArtistFacade(identifier).getArtistsAndAlbumsForOverview
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