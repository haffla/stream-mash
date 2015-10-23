package models.service

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future
import org.haffla.soundcloud.Client

object SoundcloudService extends StreamingServiceAbstract {

  val client_id_key = "soundcloud.client.id"
  val client_secret_key = "soundcloud.client.secret"

  val REDIRECT_URI="http://localhost:9000/soundcloud/callback"
  val COOKIE_KEY = "soundcloud_auth_state"

  val queryString:Map[String,Seq[String]] = Map(
    "response_type" -> Seq("code"),
    "client_id" -> Seq(CLIENT_ID),
    "redirect_uri" -> Seq(REDIRECT_URI),
    "scope" -> Seq("non-expiring")
  )

  val client = Client(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI)

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
