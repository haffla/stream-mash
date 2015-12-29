package controllers

import models.User
import models.auth.{Authenticated, AdminAccess, IdentifiedBySession, Helper}
import models.database.alias.AppDB
import models.database.facade.CollectionFacade
import models.service.analysis.SpotifyAnalysis
import models.service.library.Library
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.Controller

class UserController extends Controller {

  def list = AdminAccess.async { implicit request =>
    val users = User.list
    users.map(res => Ok(views.html.users.list(res.toList)))
  }

  def userCollectionFromDb = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    collectionFromDb(identifier)
  }

  def deleteMyCollections() = Authenticated { implicit request =>
    request.session.get("user_id") map { userId =>
      deleteCollection(userId.toInt)
    } getOrElse Ok(Json.toJson(Map("success" -> false)))
  }

  def deleteCollectionByUser(userId:Long) = AdminAccess { implicit request =>
    deleteCollection(userId.toInt)
  }

  private def deleteCollection(userId:Int) = {
    try {
      User(Left(userId)).deleteUsersCollection()
      Ok(Json.toJson(Map("success" -> true)))
    }
    catch {
      case e: Exception =>
        Ok(Json.toJson(Map("success" -> false)))
    }
  }

  private def collectionFromDb(identifier: Either[Int, String]) = {
    val library = new Library(identifier)
    AppDB.getCollectionByUser(identifier)
    CollectionFacade(identifier).userCollection map { collection =>
        if(collection.nonEmpty) Ok(library.prepareCollectionForFrontend(collection))
        else Ok(Json.toJson(Map("error" -> "You have no records stored in our database.")))
    }
  }

  def analysis = IdentifiedBySession.async { implicit request =>
    SpotifyAnalysis(Helper.getUserIdentifier(request.session)).analyse() map {
      res => Ok(res)
    }
  }
}
