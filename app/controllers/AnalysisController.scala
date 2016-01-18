package controllers

import models.auth.{Helper, IdentifiedBySession}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Controller

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait AnalysisController extends Controller {

  def artistsAndAlbumsForOverview(identifier:Either[Int,String]):Future[JsValue]

  def getArtistsForAnalysis = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    artistsAndAlbumsForOverview(identifier) map { jsonResult =>
      Ok(Json.obj("artists" -> jsonResult))
    }
  }
}
