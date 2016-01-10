package controllers

import models.User
import models.auth.{Authenticated, AdminAccess, IdentifiedBySession, Helper}
import models.database.alias.AppDB
import models.database.facade.CollectionFacade
import models.service.analysis.SpotifyAnalysis
import models.service.library.{AudioFileLibrary, Library}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.Controller

import scala.util.{Failure, Success, Try}

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
    Try {
      User(Left(userId)).deleteUsersCollection()
    } match {
      case Success(_) => Ok(Json.toJson(Map("success" -> true)))
      case Failure(e) => Ok(Json.toJson(Map("success" -> false)))
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

  def handleAudioFiles = IdentifiedBySession(parse.multipartFormData) { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    val files = request.body.files
    val allFilesAreAudioFiles = files.forall { file =>
      file.contentType match {
        case Some(t) => t.matches("audio(.*)")
        case None => false
      }
    }
    if(allFilesAreAudioFiles) {
      AudioFileLibrary(identifier).process(request.body.files)
      Ok(Json.obj("redirect" -> routes.ItunesController.index("audio").toString))
    }
    else {
      Ok(Json.obj("error" -> "One or more files are not audio files. Only audio files are accepted. Aborting."))
    }


  }
}
