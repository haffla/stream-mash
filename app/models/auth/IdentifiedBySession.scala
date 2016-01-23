package models.auth

import controllers.routes
import models.auth.form.AuthHandling
import models.service.Constants
import models.util.TextWrangler
import play.api.mvc.{ActionBuilder, Request, Result, WrappedRequest}
import play.cache.Cache

import scala.concurrent.Future

object IdentifiedBySession extends ActionBuilder[Request] with AuthHandling {
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    val userId = request.session.get(Constants.userId)
    val userName = request.session.get(Constants.username)
    (userId, userName) match {
      case (Some(uId), Some(uName)) =>
        val secretFromCache = Cache.get(s"user.$uName")
        val secretFromSession = request.session.get(Constants.authSecret).getOrElse("no-session-key")
        if(secretFromCache == secretFromSession) sessionIdentified(request, block)
        else
          redirectTo(routes.Application.index().toString, request, Constants.sessionTamperingMessage)

      case _ => sessionIdentified(request, block)
    }
  }

  def sessionIdentified[A](request:Request[A], block: (Request[A]) => Future[Result]) = {
    request.session.get(Constants.userSessionKey) match {
      case Some(sessionKey) => block(new UnAuthenticatedRequest(request))
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


