package controllers

import models.User
import models.auth.{AdminAccess, IdentifiedBySession, Helper}
import models.service.library.Library
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.Controller

import scala.concurrent.Future

class UserController extends Controller {

  def list = AdminAccess.async { implicit request =>
    val users:Future[Seq[User.Account#TableElementType]] = User.list
    users.map(
      res => Ok(views.html.users.list(res.toList))
    )
  }

  def artistAlbumCollectionFromDb = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    collectionFromDb(identifier)
  }

  private def collectionFromDb(identifier: Either[Int, String]) = {
    val library = new Library(identifier)
    library.getUsersAlbumCollection map {
      case Some(collection) => Ok(library.prepareCollectionForFrontend(collection))
      case None => Ok(Json.toJson(Map("error" -> "You have no records stored in our database.")))
    }
  }
}
