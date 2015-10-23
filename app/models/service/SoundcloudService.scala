package models.service

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future
import org.haffla.soundcloud.Client

object SoundcloudService extends StreamingServiceAbstract {

  val clientIdKey = "soundcloud.client.id"
  val clientSecretKey = "soundcloud.client.secret"

  val redirectUri="http://localhost:9000/soundcloud/callback"
  val cookieKey = "soundcloud_auth_state"

  val queryString:Map[String,Seq[String]] = Map(
    "response_type" -> Seq("code"),
    "client_id" -> Seq(clientId),
    "redirect_uri" -> Seq(redirectUri),
    "scope" -> Seq("non-expiring")
  )

  val client = Client(clientId, clientSecret, redirectUri)

  def requestUserData(code:String): Future[JsValue] = {
    for {
      authCredentials <- client.exchange_token(code)
      userId <- getUserId(authCredentials)
      response <- requestUsersTracks(userId)
    } yield response
  }

  private def requestUsersTracks(userId:String):Future[JsValue] = {
    client.users(userId)("favorites") map { favorites =>
      Json.parse(favorites)
    }
  }

  private def getUserId(authCredential: Map[String,String]):Future[String] = {
    authCredential.get("access_token") match {
      case Some(token) => client.me(token)().map { user =>
        val json = Json.parse(user)
        (json \ "id").as[Int].toString
      }
      case None => Future.failed(new Exception("A valid access token could not be retrieved"))
    }
  }

}
