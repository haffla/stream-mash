package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.service.analysis.SpotifyAnalysis
import play.api.libs.json.Json
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global

class CollectionController extends Controller {

  def index(service:String = "", openModal:String = "no") = IdentifiedBySession { implicit request =>
    Ok(views.html.collection.index(service, openModal))
  }

  def overview() = IdentifiedBySession { implicit request =>
    Ok(views.html.collection.analysis())
  }

  def analysis = IdentifiedBySession.async { implicit request =>
    SpotifyAnalysis(Helper.getUserIdentifier(request.session)).analyse() map {
      res => Ok(Json.obj("success" -> res))
    }
  }
}
