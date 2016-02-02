package models.service.oauth

import models.service.library.NapsterImporter
import models.service.oauth.NapsterService._
import models.service.util.ServiceAccessTokenHelper
import models.util.Constants
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSRequest, WS, WSResponse}

import scala.concurrent.Future

class NapsterService(identifier: Either[Int, String]) extends ApiDataRequest(Constants.serviceNapster, identifier) {

  val library = new NapsterImporter(identifier)
  override val serviceAccessTokenHelper = new ServiceAccessTokenHelper(Constants.serviceNapster, identifier)

  override def doDataRequest(code:String):Future[(Option[String],Option[String])] = {
    val data = apiEndpoints.data + ("code" -> Seq(code))
    val futureResponse: Future[WSResponse] = WS.url(apiEndpoints.token).post(data)
    for {
      token <- getAccessToken(futureResponse)
      favouriteTracks <- requestUsersTracks(token._1, 0)
      playlistTracks <- requestPlaylists(token._1)
      seq = library.convertJsonToSeq(favouriteTracks ++ playlistTracks)
      result = library.convertSeqToMap(seq)
    } yield token
  }
}

object NapsterService extends OAuthStreamingService with FavouriteMusicRetrieval with PlayListRetrieval with OauthRouting {

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
    val root = "https://api.rhapsody.com/"
    val authorize = root + "oauth/authorize"
    val tracks = root + "v1/me/favorites"
    val token = root + "oauth/access_token"
    val search = root + "v1/search"
    val artists = root + "v1/artists"
    val albums = root + "v1/albums"
    val playlists = root + "v1/me/playlists"

    val data = Map(
      "redirect_uri" -> Seq(redirectUri),
      "grant_type" -> Seq("authorization_code"),
      "response_type" -> Seq("code"),
      "client_id" -> Seq(clientId),
      "client_secret" -> Seq(clientSecret)
    )
  }

  override protected def extractTracksFromJs(trackJs: JsValue) = {
    (trackJs \ "tracks").as[JsValue]
  }

  override def favouriteMusicRetrievalRequest(accessToken:String, page:String):Future[WSResponse] =
    WS.url(apiEndpoints.tracks)
      .withQueryString("limit" -> "100", "offset" -> page)
      .withHeaders("Authorization" -> s"Bearer $accessToken").get()

  //TODO Review this
  override def getPageInformation(json: JsValue):(Boolean, Int) = {
    val list = json.as[List[JsValue]]
    (list.nonEmpty, 100)
  }

  override def authorizeEndpoint: String = apiEndpoints.authorize

  override protected def playlistRequest(accessToken: String): Future[WSResponse] = {
    WS.url(apiEndpoints.playlists)
      .withQueryString("limit" -> Constants.maxPlaylistCountToImport)
      .withHeaders("Authorization" -> s"Bearer $accessToken").get()
  }

  override protected def getNextPage(trackJs: JsValue): (Boolean, String) = {
    (false, "")
  }

  override protected def trackListLinks(js: JsValue): List[String] = {
    val list = js.as[List[JsValue]]
    list.map { item =>
      val id = (item \ "id").as[String]
      apiEndpoints.playlists + s"/$id"
    }
  }

  override protected def authenticateTrackRetrievalRequest(wsRequest: WSRequest, accessToken: String): WSRequest = {
    wsRequest.withHeaders("Authorization" -> s"Bearer $accessToken")
  }
}


