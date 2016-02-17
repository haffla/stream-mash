package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.database.facade.service.ServiceArtistTrait
import models.service.api.ApiFacade
import play.api.libs.json.Json
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait AnalysisController extends Controller {

  val serviceName:String
  val apiFacade:ApiFacade
  val serviceArtistFacade: ServiceArtistTrait

  def getArtistsForAnalysis = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    serviceArtistFacade.apply(identifier).getArtistsAndAlbumsForOverview map { jsResult =>
      Ok(Json.obj("data" -> jsResult))
    }
  }

  def getArtistDetail = IdentifiedBySession.async { implicit request =>
    request.getQueryString("id").map { spId =>
      apiFacade.getArtistInfoForFrontend(spId).map { response => Ok(response) }
    }.getOrElse(
      Future.successful(BadRequest(s"Missing parameter 'id', e.g. ${serviceName.toUpperCase} ID of the artist"))
    )
  }
}
