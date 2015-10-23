package models.service

import models.util.Logging
import play.api.Play.current
import play.api.{Play, PlayException}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}

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

  object ApiEndpoints {
    val AUTHORIZE = "https://api.soundcloud.com/connect"
    val ME = "https://api.soundcloud.com/me"
    val USERS = "http://api.soundcloud.com/users"

    val DATA = Map(
      "grant_type" -> Seq("authorization_code"),
      "redirect_uri" -> Seq(REDIRECT_URI),
      "client_id" -> Seq(CLIENT_ID),
      "client_secret" -> Seq(CLIENT_SECRET)
    )
  }

  def requestUserData(code:String): Future[Option[WSResponse]] = {
    val client = Client(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI)
    for {
      authCredentials <- client.exchange_token(code)
      userId <- getUserId(authCredentials)
      response <- requestUsersTracks(userId)
    } yield response
  }

  private def requestUsersTracks(userId:Int):Future[Option[WSResponse]] = {
    WS.url(ApiEndpoints.USERS + "/" + userId.toString + "/favorites").withQueryString("client_id" -> CLIENT_ID).get() map( response =>
      response.status match {
        case 200 =>
          val json = Json.parse(response.body)
          Some(response)
        case http_code =>
          Logging.error(ich, "Error requesting user data: " + http_code + "\n" + response.body)
          None
      }
    )
  }

  private def getUserId(authCredential: Map[String,String]):Future[Int] = {
    authCredential.get("access_token") match {
      case Some(token) =>
        WS.url(ApiEndpoints.ME).withQueryString("oauth_token" -> token).get() map { response =>
          val json = Json.parse(response.body)
          (json \ "id").as[Int]
        }
      case None => Future.failed(new Exception("A valid access token could not be retrieved"))
    }
  }

}
