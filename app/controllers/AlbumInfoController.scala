package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.database.facade.TrackFacade
import models.service.api.{ApiFacade, DeezerApiFacade}
import play.api.mvc.Controller

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait AlbumInfoController extends Controller {
  val apiFacade: ApiFacade = DeezerApiFacade
  val serviceName:String

  def getAlbumDetail = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    request.getQueryString("id").map { id =>
      val usersTracks = TrackFacade(identifier).getUsersTracks
      apiFacade.getAlbumInfoForFrontend(id,usersTracks).map { response =>
        Ok(response)
      }
    }.getOrElse(
      Future.successful(BadRequest(s"Missing parameter 'id', e.g. ${serviceName.toUpperCase} ID of the album"))
    )
  }
}
