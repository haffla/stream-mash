package controllers.auth

import play.api.mvc._

import scala.concurrent.Future

class AuthenticatedRequest[A](request: Request[A]) extends WrappedRequest[A](request)

object Authenticated extends ActionBuilder[Request] with Controller {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    request.session.get("username").map { username =>
      block(new AuthenticatedRequest(request))
    } getOrElse {
      Future.successful(Redirect(routes.AuthController.login()).flashing("message" -> "Please login in!"))
    }
  }
}
