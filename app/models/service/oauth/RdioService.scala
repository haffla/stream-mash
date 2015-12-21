package models.service.oauth

import models.auth.MessageDigest
import models.service.Constants
import models.service.library.RdioLibrary
import models.service.oauth.RdioService._
import models.service.util.ServiceAccessTokenHelper
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RdioService(identifier: Either[Int, String]) extends ApiDataRequest("rdio", identifier) {

  val library = new RdioLibrary(identifier)
  override val serviceAccessTokenHelper = new ServiceAccessTokenHelper("rdio", identifier)

  def doDataRequest(code:String) = {
    val data = apiEndpoints.data + ("code" -> Seq(code))
    val clientIdAndSecret = clientId + ":" + clientSecret
    val encodedAuthorization = MessageDigest.encodeBase64(clientIdAndSecret)
    val futureResponse: Future[WSResponse] = WS.url(apiEndpoints.token)
      .withHeaders("Authorization" -> s"Basic $encodedAuthorization").post(data)
    for {
      token <- getAccessToken(futureResponse)
      jsonResponse <- requestUsersTracks(token)
      seq = library.convertJsonToSeq(jsonResponse)
      result = library.convertSeqToMap(seq)
    } yield token
  }
}

object RdioService extends OAuthStreamingServiceAbstract with FavouriteMusicRetrieval with OauthRedirection {

  def apply(identifier: Either[Int, String]) = new RdioService(identifier)

  val clientIdKey = "rdio.client.id"
  val clientSecretKey = "rdio.client.secret"

  val redirectUriPath = "/rdio/callback"
  val cookieKey = "rdio_auth_state"

  val queryString:Map[String,Seq[String]] = Map(
    "client_id" -> Seq(clientId),
    "redirect_uri" -> Seq(redirectUri),
    "response_type" -> Seq("code")
  )

  object apiEndpoints {
    val token = "https://services.rdio.com/oauth2/token"
    val authorize = "https://www.rdio.com/oauth2/authorize"
    val mainApi = "https://services.rdio.com/api/1/"
    val getFavourites = "getFavorites"

    val data = Map(
      "redirect_uri" -> Seq(redirectUri),
      "grant_type" -> Seq("authorization_code")
    )
  }

  override def favouriteMusicRetrievalRequest(accessToken: String, page:String): Future[WSResponse] = {
    val data = Map(Constants.jsonKeyAccessToken -> Seq(accessToken), "method" -> Seq(apiEndpoints.getFavourites), "count" -> Seq("9999999999"))
    WS.url(apiEndpoints.mainApi)
      .post(data)
  }

  override def getPageInformation(json: JsValue): (Boolean, Int) = {
    (false,0)
  }
}
