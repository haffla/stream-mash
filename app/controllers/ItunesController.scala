package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsString, JsObject}
import play.api.mvc.Action
import play.api.mvc.Controller

import scala.xml.NodeSeq

class ItunesController extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.itunes.index())
  }

  def ifile = Action(parse.xml) { implicit request =>
    val xml:NodeSeq = request.body

    val jsonResponse = JsObject(
      Map("response" -> JsString("success")
      )
    )

    Ok(jsonResponse)
  }
}
