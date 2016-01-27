package models.auth.form

import models.util.Constants
import play.api.mvc.{Session, Controller, Request}
import play.cache.Cache

import scala.concurrent.Future

trait AuthHandling extends Controller {
  def redirectTo[A](page: String, request:Request[A], message:String, clean:Boolean = false) = {
    val r = Redirect(page).flashing("message" -> message)
    val redirect =
      if (clean)
        r.withNewSession
      else
        r.withSession(request.session + (Constants.intendedLocation -> request.path))
    Future.successful(redirect)
  }

  def isValidSession(username:String, session:Session): Boolean = {
    val secretFromCache = Cache.get(s"user.$username")
    val secretFromSession = session.get(Constants.authSecret).getOrElse("no-session-key")
    secretFromCache == secretFromSession
  }
}
