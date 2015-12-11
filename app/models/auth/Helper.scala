package models.auth

import models.service.Constants

object Helper {
  def getUserIdentifier(session: play.api.mvc.Session):Either[Int,String] = {
    session.get("user_id") match {
      case Some(userId) => Left(userId.toInt)
      case None =>
        session.get(Constants.userSessionKey) match {
          case Some(key) => Right(key)
          case None =>
            /** It is actually impossible that we get here as the user
              * session is set in models.auth.IdentifiedBySession
              */
            throw new Exception("")
        }
    }
  }

  def userIdentifierToString(identifier:Either[Int,String]):String = {
    identifier match {
      case Left(userId) => userId.toString
      case Right(sessionKey) => sessionKey
    }
  }
}
