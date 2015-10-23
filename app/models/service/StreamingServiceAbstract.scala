package models.service

import play.api.libs.ws.WSResponse
import play.api.{PlayException, Play}

import scala.concurrent.Future

abstract class StreamingServiceAbstract {

  val client_id_key:String
  val client_secret_key:String
  val REDIRECT_URI:String
  val COOKIE_KEY:String

  lazy val ich = this.getClass.toString

  def confErrorTitle(s:String):String = {
    s"$ich $s was not found."
  }
  def confErrorDescription(s:String, key:String):String = {
    s"Please provide a valid $s in the configuration file. Example: $key=[$s goes here]"
  }

  lazy val CLIENT_ID = Play.current.configuration.getString(client_id_key) match {
    case Some(id) => id
    case None => throw new PlayException(confErrorTitle("client id"),confErrorDescription("client id", client_id_key))
  }
  lazy val CLIENT_SECRET = Play.current.configuration.getString(client_secret_key) match {
    case Some(secret) => secret
    case None => throw new PlayException(confErrorTitle("client secret"), confErrorDescription("client secret", client_secret_key))
  }
}
