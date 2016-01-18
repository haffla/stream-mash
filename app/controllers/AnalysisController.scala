package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.service.api.{SpotifyApiFacade, ApiFacade}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Controller

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait AnalysisController extends Controller {

  val serviceName:String
  val apiFacade:ApiFacade
  def artistsAndAlbumsForOverview(identifier:Either[Int,String]):Future[JsValue]

  def getArtistsForAnalysis = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    artistsAndAlbumsForOverview(identifier) map { jsonResult =>
      Ok(Json.obj("artists" -> jsonResult))
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
