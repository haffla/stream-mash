package controllers

import java.io.File
import java.nio.file.Files

import models.User
import models.auth.{IdentifiedBySession, Helper, MessageDigest}
import models.service.library.ItunesLibrary
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.Controller

import scala.concurrent.Future

class ItunesController extends Controller {

  def index(service:String = "", openModal:String = "no") = IdentifiedBySession { implicit request =>
    Ok(views.html.itunes.index(service, openModal))
  }

  def fileUpload = IdentifiedBySession.async(parse.multipartFormData) { implicit request =>
    request.body.file("file").map { file =>
      val identifier = Helper.getUserIdentifier(request.session)
      val filename = file.filename
      val username = request.session.get("username")
        .getOrElse("user-" + System.currentTimeMillis)
      val xmlPath = s"/tmp/$filename$username"
      file.ref.moveTo(new File(xmlPath))
      val f = new File(xmlPath)
      val fileBody:String = scala.io.Source.fromFile(f).getLines().mkString
      val fileHash = MessageDigest.md5(fileBody)
      User.iTunesFileProcessedAlready(identifier,fileHash) map {
        bool => if (!bool) {
          //user has submitted the exact same file. load from db.
          User.saveItunesFileHash(identifier, fileHash)
          new ItunesLibrary(identifier, xmlPath).saveCollection()
          cleanUp(f)
        }
      }
      Future.successful(Ok(Json.toJson(Map("error" -> false))))
    }.getOrElse {
      val jsonResponse = Json.toJson(Map("error" -> "Could not read the file"))
      Future.successful(Ok(jsonResponse))
    }
  }

  private def cleanUp(f:File) = {
    Files.delete(f.toPath)
  }
}
