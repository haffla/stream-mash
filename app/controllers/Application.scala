package controllers

import models.service.api.discover.MusicBrainzApi
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global

class Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def privacy = Action { implicit request =>
    Ok(views.html.privacy())
  }

  def test = Action.async { implicit request =>
    MusicBrainzApi.findAlbumOfTrack("jealous guy", "beatles") map { res =>
      Ok(res.toString())
    }
  }
}