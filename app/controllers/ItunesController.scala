package controllers

import java.io.File
import java.nio.file.Files

import play.api.libs.json.{JsString, JsObject}
import play.api.mvc.Action
import play.api.mvc.Controller

import scala.xml.NodeSeq

class ItunesController extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.itunes.index())
  }

  def itunes= Action(parse.multipartFormData) { implicit request =>
    request.body.file("file").map { file =>
      val filename = file.filename
      val contentType = file.contentType
      val path = s"/tmp/$filename"
      file.ref.moveTo(new File(path))
      val xml = scala.xml.XML.loadFile(path)
      val dict = xml \\ "dict" \\ "dict"
      println(dict.length)
      Files.delete(new File(path).toPath)
      val jsonResponse = JsObject(
        Map(
          "response" -> JsString("success"),
          "length" -> JsString(dict.length.toString)
        )
      )
      Ok(jsonResponse)
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
