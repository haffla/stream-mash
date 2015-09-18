package controllers

import play.api.mvc._
import play.cache.Cache

import scala.concurrent.Future

class AuthenticatedRequest[A](request: Request[A]) extends WrappedRequest[A](request)

object Authenticated extends ActionBuilder[Request] with Controller {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    request.session.get("username").map { username =>
      val secretFromCache = Cache.get(s"user.$username")
      val secretFromSession = request.session.get("auth-secret").get
      println(secretFromCache)
      println(secretFromSession)
      if(secretFromCache == secretFromSession)
        block(new AuthenticatedRequest(request))
      else
        Future.successful(Redirect(routes.AuthController.login()).flashing("message" -> "The session has been tempered with."))
    } getOrElse {
      Future.successful(Redirect(routes.AuthController.login()).flashing("message" -> "Please login in!"))
    }
  }
}
