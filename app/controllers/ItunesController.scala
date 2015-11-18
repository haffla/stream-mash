package controllers

import java.io.File
import java.nio.file.Files

import models.User
import models.auth.{IdentifiedBySession, Helper, Authenticated, MessageDigest}
import models.service.Constants
import models.service.library.ItunesLibrary
import models.util.TextWrangler
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

class ItunesController extends Controller {

  def index = IdentifiedBySession { implicit request =>
    Ok(views.html.itunes.index())
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
      User.iTunesFileProcessedAlready(identifier,fileHash) flatMap(
       bool => if(bool) {
         //user has submitted the exact same file. load from db.
         cleanUp(f)
         Future.successful(Redirect(routes.UserController.artistAlbumCollectionFromDb))
       } else {
         User.saveItunesFileHash(identifier, fileHash)
         val json = collectionFromXml(identifier, xmlPath)
         cleanUp(f)
         Future.successful(Ok(json))
       })
    }.getOrElse {
      val jsonResponse = Json.toJson(Map("error" -> "Could not read the file"))
      Future.successful(Ok(jsonResponse))
    }
  }

  private def cleanUp(f:File) = {
    Files.delete(f.toPath)
  }

  private def collectionFromXml(identifier: Either[Int, String],xmlPath:String) = {
    val library = new ItunesLibrary(identifier, xmlPath)
    val collection = library.getCollection
    library.prepareCollectionForFrontend(collection)
  }
}
