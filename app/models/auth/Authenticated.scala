package models.auth

import controllers.routes
import play.api.mvc.{ActionBuilder, Controller, Request, Result, WrappedRequest}
import play.cache.Cache

import scala.concurrent.Future

object Authenticated extends ActionBuilder[Request] with Controller {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    request.session.get("username").map { username =>
      val secretFromCache = Cache.get(s"user.$username")
      val secretFromSession = request.session.get("auth-secret").getOrElse("no-session-key")
      if(secretFromCache == secretFromSession)
        block(new AuthenticatedRequest(request))
      else
        redirectToLoginPage(request, "The session has been tampered with.")
    } getOrElse {
      redirectToLoginPage(request, "Please login!")
    }
  }

  def redirectToLoginPage[A](request:Request[A], message:String) = {
    Future.successful(
      Redirect(routes.AuthController.login()).flashing("message" -> message)
        .withSession(request.session + ("intended_location" -> request.path))
      )
  }

  class AuthenticatedRequest[A](request: Request[A]) extends WrappedRequest[A](request)
}


