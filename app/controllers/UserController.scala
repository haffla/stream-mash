package controllers

import models.User
import models.auth.Authenticated
import models.service.library.Library
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class UserController extends Controller {

  def list = Authenticated.async { implicit request =>
    val users:Future[Seq[User.Account#TableElementType]] = User.list
    users.map(
      res => Ok(views.html.users.list(res.toList))
    )
  }

  def artistAlbumCollectionFromDb = Authenticated.async { implicit request =>
    val userId =request.session.get("user_id").get.toInt
    collectionFromDb(userId)
  }

  def collectionFromDb(userId:Int) = {
    val library = new Library(userId)
    library.getCollectionFromDbByUser(userId) map {
      case Some(collection) => Ok(library.prepareCollectionForFrontend(collection))
      case None => Ok(Json.toJson(Map("error" -> "You have no records stored in our database.")))
    }
  }
}
