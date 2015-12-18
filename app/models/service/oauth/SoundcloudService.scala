package models.service.oauth

import com.github.haffla.soundcloud.Client
import models.service.Constants
import models.service.library.SoundcloudLibrary
import models.service.oauth.SoundcloudService._
import models.service.util.ServiceAccessTokenHelper
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future

class SoundcloudService(identifier: Either[Int, String]) extends ApiDataRequest("soundcloud", identifier) {

  val library = new SoundcloudLibrary(identifier)
  val serviceAccessTokenCache = new ServiceAccessTokenHelper("soundcloud", identifier)

  def doDataRequest(code:String) = {
    for {
      authCredentials <- client.exchange_token(code)
      userId <- getUserId(authCredentials)
      response <- requestUsersTracks(userId)
      seq <- library.convertJsonToSeq(response)
      result = library.convertSeqToMap(seq)
    } yield (Json.parse(authCredentials) \ "access_token").asOpt[String]
  }
}

object SoundcloudService extends OAuthStreamingServiceAbstract {

  def apply(identifier: Either[Int, String]) = new SoundcloudService(identifier)

  val clientIdKey = "soundcloud.client.id"
  val clientSecretKey = "soundcloud.client.secret"

  val redirectUriPath="/soundcloud/callback"
  val cookieKey = "soundcloud_auth_state"

  val queryString:Map[String,Seq[String]] = Map(
    "response_type" -> Seq("code"),
    "client_id" -> Seq(clientId),
    "redirect_uri" -> Seq(redirectUri),
    "scope" -> Seq("non-expiring")
  )

  val client = Client(clientId, clientSecret, redirectUri)

  def requestUsersTracks(token:Option[String]):Future[Option[JsValue]] = {
    token match {
      case Some(userId) =>
        client.users(userId)("favorites") map { favorites =>
          Some(Json.parse(favorites))
        }
      case None => Future.failed(new Exception(Constants.accessTokenRetrievalError))
    }
  }

  private def getUserId(authCredentials: String):Future[Option[String]] = {
    (Json.parse(authCredentials) \ "access_token").asOpt[String] match {
      case Some(token) => client.me(token)().map { user =>
        val json = Json.parse(user)
        Some((json \ "id").as[Int].toString)
      }
      case None => Future.failed(new Exception(Constants.accessTokenRetrievalError))
    }
  }

}
