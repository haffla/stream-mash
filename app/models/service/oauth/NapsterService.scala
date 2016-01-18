package models.service.oauth

import models.service.library.NapsterLibrary
import models.service.oauth.NapsterService._
import models.service.util.ServiceAccessTokenHelper
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.Future

class NapsterService(identifier: Either[Int, String]) extends ApiDataRequest("napster", identifier) {

  val library = new NapsterLibrary(identifier)
  override val serviceAccessTokenHelper = new ServiceAccessTokenHelper("napster", identifier)

  override def doDataRequest(code:String):Future[(Option[String],Option[String])] = {
    val data = apiEndpoints.data + ("code" -> Seq(code))
    val futureResponse: Future[WSResponse] = WS.url(apiEndpoints.token).post(data)
    for {
      token <- getAccessToken(futureResponse)
      response <- requestUsersTracks(token._1, 0)
      seq = library.convertJsonToSeq(response)
      result = library.convertSeqToMap(seq)
    } yield token
  }
}

object NapsterService extends OAuthStreamingServiceAbstract with FavouriteMusicRetrieval with OauthRedirection {

  def apply(identifier: Either[Int, String]) = new NapsterService(identifier)
  val clientIdKey = "napster.client.id"
  val clientSecretKey = "napster.client.secret"
  val redirectUriPath = "/napster/callback"
  val cookieKey = "napster_auth_state"

  val queryString:Map[String,Seq[String]] = Map(
    "response_type" -> Seq("code"),
    "client_id" -> Seq(clientId),
    "redirect_uri" -> Seq(redirectUri)
  )

  object apiEndpoints {
    val authorize = "https://api.rhapsody.com/oauth/authorize"
    val tracks = "https://api.rhapsody.com/v1/me/favorites"
    val token = "https://api.rhapsody.com/oauth/access_token"
    val search = "http://api.rhapsody.com/v1/search"
    val artists = "http://api.rhapsody.com/v1/artists"
    val albums = "http://api.rhapsody.com/v1/albums"

    val data = Map(
      "redirect_uri" -> Seq(redirectUri),
      "grant_type" -> Seq("authorization_code"),
      "response_type" -> Seq("code"),
      "client_id" -> Seq(clientId),
      "client_secret" -> Seq(clientSecret)
    )
  }

  override def favouriteMusicRetrievalRequest(accessToken:String, page:String):Future[WSResponse] =
    WS.url(apiEndpoints.tracks)
      .withQueryString("limit" -> "100", "offset" -> page)
      .withHeaders("Authorization" -> s"Bearer $accessToken").get()

  //TODO Review this
  override def getPageInformation(json: JsValue): (Boolean, Int) = {
    val list = json.as[List[JsValue]]
    (list.nonEmpty, list.length)
  }
}


