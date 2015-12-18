import models.messaging.listen.ArtistIdListener
import play.api.mvc.RequestHeader
import play.api.mvc.Results._

import scala.concurrent.Future


object Global extends play.api.GlobalSettings {

  override def onStart(app: play.api.Application) {
    ArtistIdListener.listen()
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    val exceptionMessage = ex.getMessage
    Future.successful(InternalServerError(views.html.error(
      Map(
        "exceptionMessage" -> exceptionMessage
      )
    )))
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    val path = request.path
    Future.successful(NotFound(views.html.error(
      Map(
        "title" -> "Oups. The page you were looking for does not exist.",
        "path" -> path
      )
    )))
  }
}