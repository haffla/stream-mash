package models.auth

import models.service.Constants
import models.util.TextWrangler
import play.api.mvc.{ActionBuilder, Controller, Request, Result, WrappedRequest}

import scala.concurrent.Future

object IdentifiedBySession extends ActionBuilder[Request] with Controller {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    request.session.get(Constants.userSessionKey) match {
      case Some(sessionKey) =>
        block(new UnAuthenticatedRequest(request))
      case None =>
        Future.successful(
          Redirect(request.path)
            .withSession(
              request.session + (Constants.userSessionKey -> TextWrangler.generateRandomString(32))
            )
        )
    }
  }

  class UnAuthenticatedRequest[A](request: Request[A]) extends WrappedRequest[A](request)
}


