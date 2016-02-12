package models.auth

import controllers.routes
import models.auth.form.AuthHandling
import models.util.{Constants, TextWrangler}
import play.api.mvc.{ActionBuilder, Request, Result, WrappedRequest}
import play.cache.Cache

import scala.concurrent.Future

object IdentifiedBySession extends ActionBuilder[Request] with AuthHandling {
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    val userId = request.session.get(Constants.userId)
    val userName = request.session.get(Constants.username)
    userId match {
      case Some(uId)=>
        userName match {
          case Some(uName) =>
            if(isValidSession(uName, request.session)) sessionIdentified(request, block)
            else
              redirectTo(routes.Application.index().toString, request, Constants.sessionTamperingMessage, clean = true)
          case _ =>
            redirectTo(routes.Application.index().toString, request, Constants.sessionTamperingMessage, clean = true)
        }
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
              request.session + (Constants.userSessionKey -> TextWrangler.generateRandomString(32)) + (Constants.intendedLocation -> request.path)
            )
        )
    }
  }

  class UnAuthenticatedRequest[A](request: Request[A]) extends WrappedRequest[A](request)
}


