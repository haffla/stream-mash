package controllers

import java.io.File

import models.User
import models.auth.{Helper, IdentifiedBySession, MessageDigest}
import models.service.library.ItunesLibrary
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.Controller

import scala.concurrent.Future

class ItunesController extends Controller {

  def fileUpload = IdentifiedBySession.async(parse.multipartFormData) { implicit request =>
    request.body.file("file").map { file =>
      val identifier = Helper.getUserIdentifier(request.session)
      val filename = file.filename
      val username = request.session.get("username")
        .getOrElse("user-" + System.currentTimeMillis)
      val xmlPath = s"/tmp/$filename$username"
      file.ref.moveTo(new File(xmlPath))
      val fileBody:String = scala.io.Source.fromFile(xmlPath).getLines().mkString
      val fileHash = MessageDigest.md5(fileBody)
      val userModel = User(identifier)
      userModel.iTunesFileProcessedAlready(fileHash) map {
        bool => if (!bool) {
          //user has submitted the exact same file. load from db.
          userModel.saveItunesFileHash(fileHash)
          new ItunesLibrary(identifier, xmlPath).saveCollection()
        }
      }

      Future.successful(Ok(Json.obj("error" -> false, "redirect" -> routes.CollectionController.index().toString)))
    }.getOrElse {
      val jsonResponse = Json.toJson(Map("error" -> "Could not read the file"))
      Future.successful(Ok(jsonResponse))
    }
  }
}
