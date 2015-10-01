package controllers

import play.api.mvc.Controller

class Application extends Controller {

  def index = Authenticated { implicit request =>
    Ok(views.html.index())
  }
}