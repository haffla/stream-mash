package controllers

import models.User
import models.auth.{AdminAccess, Authenticated, Helper, IdentifiedBySession}
import models.database.facade.CollectionFacade
import models.service.analysis.ServiceAnalyser
import models.service.exporter.Exporter
import models.service.visualization.ServiceData
import models.util.Constants
import play.api.libs.json.Json
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}

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
      success <- ServiceAnalyser(identifier).analyse()
    } yield Ok(Json.obj("success" -> success))
  }

  def visualizationData = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    new ServiceData(identifier).retrieve().map { res =>
      Ok(res)
    }
  }

  def userCollectionFromDb = IdentifiedBySession { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    val collection = CollectionFacade(identifier).userCollection
    if(collection.nonEmpty) Ok(Exporter.prepareCollectionForFrontend(collection))
    else Ok(Json.toJson(Map("error" -> "You have no records stored in our database.")))
  }

  def deleteMyCollections() = Authenticated { implicit request =>
    request.session.get(Constants.userId) map { userId =>
      deleteCollection(userId.toInt)
    } getOrElse Ok(Json.toJson(Map("success" -> false)))
  }

  def deleteCollectionByUser(userId:Long) = AdminAccess { implicit request =>
    deleteCollection(userId.toInt)
  }

  private def deleteCollection(userId:Int) = {
    val success = Try {
      User(Left(userId)).deleteUsersCollection()
    } match {
      case Success(_) => true
      case _ => false
    }
    Ok(Json.toJson(Map("success" -> success)))
  }
}
