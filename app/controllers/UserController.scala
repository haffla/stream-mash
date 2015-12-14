package controllers

import models.User
import models.auth.{Authenticated, AdminAccess, IdentifiedBySession, Helper}
import models.service.library.Library
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.Controller

import scala.concurrent.Future

class UserController extends Controller {

  def list = AdminAccess.async { implicit request =>
    val users:Future[Seq[User.Account#TableElementType]] = User.list
    users.map(res => Ok(views.html.users.list(res.toList)))
  }

  def artistAlbumCollectionFromDb = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    collectionFromDb(identifier)
  }

  def deleteMyCollections() = Authenticated.async { implicit request =>
    request.session.get("user_id") map { userId =>
      User.deleteUsersAlbumCollection(userId.toInt) map { count =>
        Ok(Json.toJson(Json.toJson(Map("success" -> true))))
      }
    } getOrElse Future.successful(Ok(Json.toJson(Map("success" -> false))))
  }

  def deleteCollectionByUser(userId:Int) = AdminAccess.async { implicit request =>
    User.deleteUsersAlbumCollection(userId) map { count =>
      Ok(Json.toJson(Map("success" -> true)))
    }
  }

  private def collectionFromDb(identifier: Either[Int, String]) = {
    val library = new Library(identifier)
    library.getUsersAlbumCollection map {
      case Some(collection) => Ok(library.prepareCollectionForFrontend(collection))
      case None => Ok(Json.toJson(Map("error" -> "You have no records stored in our database.")))
    }
  }
}
