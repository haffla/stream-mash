package controllers

import java.io.File
import java.nio.file.Files
import models.User
import models.auth.MessageDigest

import scala.concurrent.Future

import models.util.ItunesLibrary
import play.api.mvc.Controller
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class ItunesController extends Controller {

  def index = Authenticated { implicit request =>
    Ok(views.html.itunes.index())
  }

  def getArtistsFromXML(save:Boolean = true) = Authenticated(parse.multipartFormData) { implicit request =>
    request.body.file("file").map { file =>
      val userId:Int = request.session.get("user_id").get.toInt
      println(userId)
      val filename = file.filename
      val username = request.session.get("username")
        .getOrElse("user-" + System.currentTimeMillis)
      val path = s"/tmp/$filename$username"
      file.ref.moveTo(new File(path))
      val f = new File(path)
      if(save) {
        val fileBody:String = scala.io.Source.fromFile(f).getLines().mkString
        val fileHash = MessageDigest.md5(fileBody)
        User.iTunesFileProcessedAlready(userId,fileHash).map(
         bool => if(bool) User.saveItunesFileHash(userId, fileHash)
        )
      }
      //TODO: only process file if it hadnt already processed and if there is no data in database

      val library = new ItunesLibrary(userId.toInt, save)
      val parsedXml = library.parseXml(path)
      val artists = library.getLibrary(parsedXml)
      val json = Json.toJson(artists)
      Files.delete(f.toPath)
      Ok(json)
    }.getOrElse {
      val jsonResponse = Json.toJson(Map("response" -> "Could not read the file"))
      Ok(jsonResponse)
    }
  }

  def getArtistsFromDb = Authenticated.async { implicit request =>
    val user_id =request.session.get("user_id").get.toInt
    val library = new ItunesLibrary(user_id)
    library.getAlbumsByUser(user_id).map ({
      case Some(art) => Ok(Json.toJson(art))
      case None => Ok(Json.toJson(Map("response" -> "No albums found for user.")))
    })
  }
}
