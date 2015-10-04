package controllers

import java.io.File
import java.nio.file.Files
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
      val user_id =request.session.get("user_id").get.toInt
      val filename = file.filename
      val username = request.session.get("username")
        .getOrElse("user-" + System.currentTimeMillis)
      val path = s"/tmp/$filename$username"
      file.ref.moveTo(new File(path))
      val library = new ItunesLibrary(user_id, save)
      val parsedXml = library.parseXml(path)
      val artists = library.getLibrary(parsedXml)
      val json = Json.toJson(artists)
      Files.delete(new File(path).toPath)
      Ok(json)
    }.getOrElse {
      val jsonResponse = Json.toJson(Map("response" -> "Could not read the file"))
      Ok(jsonResponse)
    }
  }

  def getArtistsFromDb = Authenticated.async { implicit request =>
    println("Hello")
    val user_id =request.session.get("user_id").get.toInt
    val library = new ItunesLibrary(user_id)
    library.getAlbumsByUser(user_id).map ({
      case Some(art) => Ok(Json.toJson(art))
      case None => Ok(Json.toJson(Map("response" -> "No albums found for user.")))
    })
  }
}
