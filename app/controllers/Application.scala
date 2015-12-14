package controllers

import models.auth.Helper
import models.service.api.discover.{RetrievalProcessMonitor, MusicBrainzApi}
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

  def socket = WebSocket.using[String] { request =>
    val (out, channel) = Concurrent.broadcast[String]
    val identifier = Helper.getUserIdentifier(request.session)
    val in = Iteratee.foreach[String] { service =>
        val apiHelper = new RetrievalProcessMonitor(service, identifier)
        apiHelper.waitForRetrievalProcessToBeDone(channel, 1000)
    }
    (in, out)
  }
}