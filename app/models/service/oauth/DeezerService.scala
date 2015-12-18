package models.service.oauth

import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DeezerService extends OAuthStreamingServiceAbstract with FavouriteMusicRetrieval with OauthRedirection {

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

  override def favouriteMusicRetrievalRequest(accessToken: String): Future[WSResponse] =
    WS.url(apiEndpoints.tracks).withQueryString("access_token" -> accessToken).get()
}
