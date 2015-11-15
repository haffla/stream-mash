package models.service.oauth

import models.service.Constants
import models.util.Logging
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DeezerService extends StreamingServiceAbstract {

  val clientIdKey = "deezer.app.id"
  val clientSecretKey = "deezer.app.secret"

  val redirectUriPath = "/deezer/callback"
  val cookieKey = "deezer_auth_state"

  val queryString:Map[String,Seq[String]] = Map(
    "app_id" -> Seq(clientId),
    "redirect_uri" -> Seq(redirectUri),
    "perms" -> Seq("basic_access, listening_history")
  )

  object apiEndpoints {
    val token = "https://connect.deezer.com/oauth/access_token.php"
    val authorize = "https://connect.deezer.com/oauth/auth.php"
    val tracks = "http://api.deezer.com/user/me/tracks"
  }

  def requestUserData(code:String):Future[Option[JsValue]] = {
    val futureResponse: Future[WSResponse] = WS.url(apiEndpoints.token).withQueryString(
      "app_id" -> clientId,
      "secret" -> clientSecret,
      "code" -> code,
      "output" -> "json").get()

    for {
      token <- getAccessToken(futureResponse)
      response <- requestUsersTracks(token)
    } yield response
  }

  private def requestUsersTracks(token:Option[String]):Future[Option[JsValue]] = {
    token match {
      case Some(access_token) =>
        WS.url(apiEndpoints.tracks).withQueryString("access_token" -> access_token).get() map {
          response =>
            response.status match {
              case 200 =>
                val json = Json.parse(response.body)
                Some(json)
              case http_code =>
                Logging.error(ich, Constants.userTracksRetrievalError + ": " + http_code + "\n" + response.body)
                None
            }
        }
      case None => throw new Exception(Constants.accessTokenRetrievalError)
    }
  }
}
