package models.service.oauth

import models.service.Constants
import models.util.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import play.api.{Play, PlayException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class OAuthStreamingService {

  val clientIdKey:String
  val clientSecretKey:String

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

  def getAccessToken(futureReponse: Future[WSResponse]): Future[(Option[String],Option[String])] = {
    futureReponse.map(response =>
      response.status match {
        case 200 =>
          val json = Json.parse(response.body)
          val accessToken = (json \ Constants.jsonKeyAccessToken).asOpt[String]
          val refreshToken = (json \ Constants.jsonKeyRefreshToken).asOpt[String]
          (accessToken,refreshToken)
        case http_code =>
          Logging.error(ich, Constants.accessTokenRetrievalError + ": " + http_code + "\n" + response.body)
          (None,None)
      }
    )
  }
}
