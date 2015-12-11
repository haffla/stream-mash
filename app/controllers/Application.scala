package controllers

import models.auth.Helper
import models.service.api.discover.{ApiHelper, MusicBrainzApi}
import play.api.libs.iteratee.{Concurrent, Iteratee}
import play.api.mvc.{WebSocket, Action, Controller}
import util.control.Breaks._

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
    val in = Iteratee.foreach[String] {
      msg =>
        val apiHelper = new ApiHelper(msg, Helper.getUserIdentifier(request.session))
        breakable {
          while(true) {
            apiHelper.getRetrievalProcessStatus match {
              case Some(status) =>
                channel push status.toString
                if(status == "done") {
                  break()
                }
                else {
                  Thread.sleep(1000)
                }
              case None =>
                channel push "done"
                break()
            }
          }
        }
    }
    (in, out)
  }
}