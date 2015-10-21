package models.auth

import controllers.routes
import play.api.mvc._
import play.cache.Cache

import scala.concurrent.Future

class AuthenticatedRequest[A](request: Request[A]) extends WrappedRequest[A](request)

object Authenticated extends ActionBuilder[Request] with Controller {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    request.session.get("username").map { username =>
      val secretFromCache = Cache.get(s"user.$username")
      val secretFromSession = request.session.get("auth-secret").getOrElse("no-session-key")
      if(secretFromCache == secretFromSession)
        block(new AuthenticatedRequest(request))
      else
        redirectToLoginPage("The session has been tampered with.")
    } getOrElse {
      redirectToLoginPage("Please login!")
    }
  }

  def redirectToLoginPage(message:String) = {
    Future.successful(Redirect(routes.AuthController.login()).flashing("message" -> message))
  }
}


