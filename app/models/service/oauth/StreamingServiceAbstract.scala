package models.service.oauth

import models.service.Constants
import models.util.Logging
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.{Play, PlayException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class StreamingServiceAbstract {

  val clientIdKey:String
  val clientSecretKey:String
  val cookieKey:String
  val redirectUriPath:String

  lazy val ich = this.getClass.toString

  def confErrorTitle(s:String):String = {
    s"$ich $s was not found."
  }
  def confErrorDescription(s:String, key:String):String = {
    s"Please provide a valid $s in the configuration file. Example: $key=[$s goes here]"
  }

  lazy val clientId = Play.current.configuration.getString(clientIdKey) match {
    case Some(id) => id
    case None => throw new PlayException(confErrorTitle("client id"), confErrorDescription("client id", clientIdKey))
  }
  lazy val clientSecret = Play.current.configuration.getString(clientSecretKey) match {
    case Some(secret) => secret
    case None => throw new PlayException(confErrorTitle("client secret"), confErrorDescription("client secret", clientSecretKey))
  }

  lazy val redirectUri = Play.current.configuration.getString("current.host") match {
    case Some(uri) => uri + redirectUriPath
    case None =>
      println("Current host could not be determined. It is used for all callbacks from third party APIs" + "\n" +
      "Example: current.host=\"http://example.com\". Falling back to development uri http://localhost:9000")

      "http://localhost:9000" + redirectUriPath

  }

  def getAccessToken(futureReponse: Future[WSResponse]): Future[Option[String]] = {
    futureReponse.map(response =>
      response.status match {
        case 200 =>
          val json = Json.parse(response.body)
          (json \ Constants.jsonKeyAccessToken).asOpt[String]
        case http_code =>
          Logging.error(ich, Constants.accessTokenRetrievalError + ": " + http_code + "\n" + response.body)
          None
      }
    )
  }
}
