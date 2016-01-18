package models.service.oauth

import models.service.library.DeezerLibrary
import models.service.oauth.DeezerService._
import models.service.util.ServiceAccessTokenHelper
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeezerService(identifier:Either[Int,String]) extends ApiDataRequest("deezer", identifier) {

  override val serviceAccessTokenHelper: ServiceAccessTokenHelper = new ServiceAccessTokenHelper("deezer", identifier)
  val library = new DeezerLibrary(identifier)

  override def doDataRequest(code: String): Future[(Option[String],Option[String])] = {
    val futureResponse: Future[WSResponse] = WS.url(apiEndpoints.token).withQueryString(
      "app_id" -> clientId,
      "secret" -> clientSecret,
      "code" -> code,
      "output" -> "json").get()

    for {
      token <- getAccessToken(futureResponse)
      response <- requestUsersTracks(token._1)
      seq = library.convertJsonToSeq(response)
      res = library.convertSeqToMap(seq)
    } yield token
  }
}
object DeezerService extends OAuthStreamingService with FavouriteMusicRetrieval with OauthRouting {

  def apply(identifier:Either[Int,String]) = new DeezerService(identifier)

  val clientIdKey = "deezer.app.id"
  val clientSecretKey = "deezer.app.secret"

  val redirectUriPath = "/deezer/callback"
  val cookieKey = "deezer_auth_state"

  override lazy val redirectUri = "http://haffla.de"

  val queryString:Map[String,Seq[String]] = Map(
    "app_id" -> Seq(clientId),
    "redirect_uri" -> Seq(redirectUri),
    "perms" -> Seq("basic_access, listening_history")
  )

  object apiEndpoints {
    val token = "https://connect.deezer.com/oauth/access_token.php"
    val authorize = "https://connect.deezer.com/oauth/auth.php"
    val tracks = "http://api.deezer.com/user/me/tracks"
    val albums = "http://api.deezer.com/album"
    val artists = "http://api.deezer.com/artist"
    val search = "http://api.deezer.com/search"
  }

  override def favouriteMusicRetrievalRequest(accessToken: String, page:String): Future[WSResponse] =
    WS.url(apiEndpoints.tracks).withQueryString("access_token" -> accessToken).get()

  override def getPageInformation(js:JsValue):(Boolean,Int) = {
    //TODO: Implement
    (false,0)
  }

  override def authorizeEndpoint: String = apiEndpoints.authorize
}
