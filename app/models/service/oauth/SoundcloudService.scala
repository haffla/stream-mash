package models.service.oauth

import com.github.haffla.soundcloud.Client
import models.service.Constants
import models.service.library.SoundcloudLibrary
import models.service.oauth.SoundcloudService._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future

class SoundcloudService(identifier: Either[Int, String]) {

  val library = new SoundcloudLibrary(identifier)

  def requestUserData(code:String): Future[JsValue] = {
    for {
      authCredentials <- client.exchange_token(code)
      userId <- getUserId(authCredentials)
      response <- requestUsersTracks(userId)
      seq = library.convertJsonToSeq(response)
      result = library.convertSeqToMap(seq)
    } yield library.prepareCollectionForFrontend(result)
  }
}

object SoundcloudService extends StreamingServiceAbstract {

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

  private def requestUsersTracks(userId:String):Future[Option[JsValue]] = {
    client.users(userId)("favorites") map { favorites =>
      Some(Json.parse(favorites))
    }
  }

  private def getUserId(authCredentials: String):Future[String] = {
    (Json.parse(authCredentials) \ "access_token").asOpt[String] match {
      case Some(token) => client.me(token)().map { user =>
        val json = Json.parse(user)
        (json \ "id").as[Int].toString
      }
      case None => Future.failed(new Exception(Constants.accessTokenRetrievalError))
    }
  }

}
