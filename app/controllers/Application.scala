package controllers

import models.auth.Helper
import models.service.api.discover.{ApiHelper, MusicBrainzApi}
import play.api.libs.iteratee.{Concurrent, Iteratee}
import play.api.mvc.{WebSocket, Action, Controller}

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

  def socket = WebSocket.using[String] { request =>
    val (out, channel) = Concurrent.broadcast[String]
    val in = Iteratee.foreach[String] { service =>
        val apiHelper = new ApiHelper(service, Helper.getUserIdentifier(request.session))
        // Wait a maximum of 2 minutes
        (1 to 120).toStream.takeWhile { _ =>
          !apiHelper.retrievalProcessIsDone(channel, 1000)
        } foreach( _ => apiHelper.getRetrievalProcessStatus)
    }
    (in, out)
  }
}