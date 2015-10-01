package controllers

import java.io.File
import java.nio.file.Files

import models.util.ItunesLibrary
import play.api.libs.json.{JsString, JsObject}
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.json._

class ItunesController extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.itunes.index())
  }

  def itunes= Action(parse.multipartFormData) { implicit request =>
    request.body.file("file").map { file =>
      val filename = file.filename
      val username = request2session.get("username")
        .getOrElse("user-" + System.currentTimeMillis)
      val path = s"/tmp/$filename$username"
      file.ref.moveTo(new File(path))
      val library = new ItunesLibrary(path)
      val parsedXml = library.parseXml()
      val artists = library.getLibrary(parsedXml)
      val json = Json.toJson(artists)

      Files.delete(new File(path).toPath)
      Ok(json)
    }.getOrElse {
      val jsonResponse = JsObject(
        Map(
          "response" -> JsString("error")
        )
      )
      Ok(jsonResponse)
    }
  }
}
