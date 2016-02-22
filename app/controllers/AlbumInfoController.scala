package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.database.facade.TrackFacade
import models.service.api.{ApiFacade, DeezerApiFacade}
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait AlbumInfoController extends Controller {
  val apiFacade: ApiFacade = DeezerApiFacade
  val serviceName:String

  def getAlbumDetail = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    val albumName = request.getQueryString("name")
    val albumId = request.getQueryString("id")
    (albumName,albumId) match {
      case (Some(name),Some(id)) =>
        val usersTracks = TrackFacade(identifier).getUsersTracksForAlbum(name)
        apiFacade.getAlbumInfoForFrontend(id,usersTracks).map { response =>
          Ok(response)
        }
      case _ =>
        Future.successful(BadRequest(s"Missing parameter 'id' or 'name' for service $serviceName"))
    }
  }
}
