package models.service.oauth

import models.util.Logging
import play.api.Play

trait OauthRedirection {

  val cookieKey:String
  val redirectUriPath:String

  lazy val redirectUri = Play.current.configuration.getString("current.host") match {
    case Some(uri) => uri + redirectUriPath
    case None =>
      Logging.info(this.getClass.toString, "Current host could not be determined. It is used for all callbacks from third party APIs" + "\n" +
        "Example: current.host=\"http://example.com\". Falling back to development uri http://localhost:9000")

      "http://localhost:9000" + redirectUriPath
  }
}
