package models.service.oauth

import models.auth.MessageDigest
import models.service.Constants
import models.service.library.LastfmLibrary
import models.service.oauth.LastfmService.apiEndpoints
import models.service.util.ServiceAccessTokenHelper
import models.util.Logging
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WS, WSResponse}
import models.service.oauth.LastfmService._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LastfmService(identifier: Either[Int, String]) extends ApiDataRequest  ("lastfm", identifier) {

  val library = new LastfmLibrary(identifier)
  override val serviceAccessTokenHelper = new ServiceAccessTokenHelper("lastfm", identifier)

  def generateApiSig(code: String) = {
    MessageDigest.md5("api_key" + clientId + "method" + apiEndpoints.authMethod + "token" + code + clientSecret)
  }

  def doDataRequest(code:String) = {
    val apiSig:String = generateApiSig(code)
    val data = apiEndpoints.data + ("token" -> Seq(code), "api_sig" -> Seq(apiSig))
    val futureResponse: Future[WSResponse] = WS.url(apiEndpoints.mainApi).post(data)
    for {
      credentials <- getAccessToken(futureResponse)
      (username,token) = credentials match {
        case Some(secret) =>
          val split = secret.split(LastfmService.credentialDevider)
          (Some(split.head), Some(split(1)))
        case None => (None,None)
      }
      response <- requestUsersTracks(username)
      seq = library.convertJsonToSeq(response)
      res = library.convertSeqToMap(seq)
    } yield {
      token
    }
  }

}

object LastfmService extends OAuthStreamingServiceAbstract with FavouriteMusicRetrieval with OauthRedirection {

  def apply(identifier: Either[Int, String]) = new LastfmService(identifier)

  val clientIdKey = "lastfm.client.id"
  val clientSecretKey = "lastfm.client.secret"

  val redirectUriPath = "/lastfm/callback"
  val cookieKey = "lastfm_auth_state"

  val queryString:Map[String,Seq[String]] = Map(
    "api_key" -> Seq(clientId),
    "cb" -> Seq(redirectUri)
  )

  object apiEndpoints {
    val authorize = "http://www.last.fm/api/auth"
    val mainApi = "http://ws.audioscrobbler.com/2.0/"
    val getFavourites = "user.getTopTracks"
    val authMethod = "auth.getSession"

    val data = Map(
      "method" -> Seq("auth.getSession"),
      "api_key" -> Seq(clientId),
      "format" -> Seq("json")

    )
  }

  val credentialDevider = "DIVIDER"

  override def getAccessToken(futureReponse: Future[WSResponse]): Future[Option[String]] = {
    futureReponse.map(response =>
      response.status match {
        case 200 =>
          val json = Json.parse(response.body)
          (json \ "session" \ "name").asOpt[String] match {
            case Some(name) => Some(name + credentialDevider + (json \ "session" \ "key").as[String])
            case None => None
          }
        case http_code =>
          Logging.error(ich, Constants.accessTokenRetrievalError + ": " + http_code + "\n" + response.body)
          None
      }
    )
  }

  override def favouriteMusicRetrievalRequest(username: String, page:String): Future[WSResponse] = {
    WS.url(apiEndpoints.mainApi)
      .withQueryString("method" -> apiEndpoints.getFavourites, "api_key" -> clientId, "format" -> "json", "user" -> username, "page" -> page)
      .get()
  }

  override def getPageInformation(json:JsValue):(Boolean,Int) = {
    val page = (json \ "toptracks" \ "@attr" \ "page").as[String].toInt
    val total = (json \"toptracks" \ "@attr" \ "totalPages").as[String].toInt
    (page < total, page + 1)
  }
}
