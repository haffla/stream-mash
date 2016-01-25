package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.service.analysis.ServiceAnalyser
import play.api.libs.json.Json
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CollectionController extends Controller {

  def index(service:String = "") = IdentifiedBySession { implicit request =>
    Ok(views.html.collection.index(service))
  }

  def overview() = IdentifiedBySession { implicit request =>
    Ok(views.html.collection.overview())
  }

  def visualize() = IdentifiedBySession { implicit request =>
    Ok(views.html.collection.visualize())
  }

  def analysis = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    for {
      result <- ServiceAnalyser(identifier).analyse()
    } yield Ok(Json.obj("success" -> result))
  }

  def visualizationData = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    Future.successful(Ok(""))
  }
}
