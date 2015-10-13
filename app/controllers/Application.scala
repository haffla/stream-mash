package controllers

import play.api.mvc.{Action, Controller}

class Application extends Controller {

  def index = Authenticated { implicit request =>
    Ok(views.html.index())
  }

  def privacy = Action {
    Ok(views.html.privacy())
  }
}