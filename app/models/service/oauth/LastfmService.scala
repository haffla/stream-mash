package models.service.oauth

import models.auth.MessageDigest
import models.service.importer.LastfmImporter
import models.service.oauth.LastfmService.{apiEndpoints, _}
import models.service.util.ServiceAccessTokenHelper
import models.util.{Constants, Logging}
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LastfmService(identifier: Either[Int, String]) extends ApiDataRequest(Constants.serviceLastFm, identifier) {

  override val importer = new LastfmImporter(identifier)
  override val serviceAccessTokenHelper = new ServiceAccessTokenHelper(Constants.serviceLastFm, identifier)

  def generateApiSig(code: String) = {
    MessageDigest.md5("api_key" + clientId + "method" + apiEndpoints.authMethod + "token" + code + clientSecret)
  }

  override def doDataRequest(code:String):Future[(Option[String],Option[String])] = {
    val apiSig:String = generateApiSig(code)
    val data = apiEndpoints.data + ("token" -> Seq(code), "api_sig" -> Seq(apiSig))
    val futureResponse: Future[WSResponse] = WS.url(apiEndpoints.mainApi).post(data)
    for {
      (credentials,_) <- getAccessToken(futureResponse)
      (username,token) = credentials match {
        case Some(secret) =>
          val split = secret.split(LastfmService.credentialDevider)
          (Some(split.head), Some(split(1)))
        case None => (None,None)
      }
      response <- requestUsersTracks(username)
      seq = importer.convertJsonToSeq(response)
      res = importer.convertSeqToMap(seq)
    } yield (token,None)
  }

}

object LastfmService extends OAuthStreamingService with FavouriteMusicRetrieval with OAuthRouting {

  def apply(identifier: Either[Int, String]) = new LastfmService(identifier)

  override val clientIdKey = "lastfm.client.id"
  override val clientSecretKey = "lastfm.client.secret"
  override val redirectUriPath = "/lastfm/callback"
  override val cookieKey = "lastfm_auth_state"

  override val queryString:Map[String,Seq[String]] = Map(
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

  override def getAccessToken(futureReponse: Future[WSResponse]): Future[(Option[String],Option[String])] = {
    futureReponse.map(response =>
      response.status match {
        case 200 =>
          val json = Json.parse(response.body)
          (json \ "session" \ "name").asOpt[String] match {
            case Some(name) => (Some(name + credentialDevider + (json \ "session" \ "key").as[String]),None)
            case None => (None,None)
          }
        case http_code =>
          Logging.error(ich, Constants.accessTokenRetrievalError + ": " + http_code + "\n" + response.body)
          (None,None)
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

  override val authorizeEndpoint: String = apiEndpoints.authorize
}
