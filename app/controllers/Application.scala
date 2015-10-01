package controllers

import play.api.libs.json._
import play.api.mvc.Controller

class Application extends Controller {

  var commentsJson: JsArray = JsArray(
    List(
      JsObject(
        Map(
          "author" -> JsString("Peter Affe"),
          "text" -> JsString("Hello you there how are you?")
        )
      )
    )
  )

  def index = Authenticated { implicit request =>
    Ok(views.html.index())
  }
}