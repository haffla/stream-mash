package models.service.oauth

import models.service.importer.SpotifyImporter
import models.service.oauth.SpotifyService._
import models.service.util.ServiceAccessTokenHelper
import models.util.Constants
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSRequest, WS, WSResponse}

import scala.concurrent.Future

class SpotifyService(identifier: Either[Int, String]) extends ApiDataRequest(Constants.serviceSpotify, identifier) {

  override val importer = new SpotifyImporter(identifier)
  override val serviceAccessTokenHelper = new ServiceAccessTokenHelper(Constants.serviceSpotify, identifier)

  override def doDataRequest(code:String):Future[(Option[String],Option[String])] = {
    val data = apiEndpoints.data + ("code" -> Seq(code))
    val futureResponse: Future[WSResponse] = WS.url(apiEndpoints.token).post(data)
    for {
      token <- getAccessToken(futureResponse)
      userTracks <- requestUsersTracks(token._1)
      playlistTracks <- requestPlaylists(token._1)
      seq = importer.convertJsonToSeq(userTracks ++ playlistTracks)
      result = importer.convertSeqToMap(seq)
    } yield token
  }
}

object SpotifyService extends OAuthStreamingService with FavouriteMusicRetrieval with PlayListRetrieval with OAuthRouting {

  def apply(identifier: Either[Int, String]) = new SpotifyService(identifier)
  override val clientIdKey = "spotify.client.id"
  override val clientSecretKey = "spotify.client.secret"
  override val redirectUriPath = "/spotify/callback"
  val scope:Seq[String] = Seq(
    "user-read-private",
    "playlist-read-private",
    "user-library-read",
    "playlist-read-collaborative"
  )
  override val cookieKey = "spotify_auth_state"

  override val authorizeEndpoint: String = apiEndpoints.authorize

  override val queryString:Map[String,Seq[String]] = Map(
    "response_type" -> Seq("code"),
    "client_id" -> Seq(clientId),
    "scope" -> Seq(scope.mkString(" ")),
    "redirect_uri" -> Seq(redirectUri)
  )

  object apiEndpoints {
    val root = "https://api.spotify.com/v1/"
    val tracks = root + "me/tracks"
    val token = "https://accounts.spotify.com/api/token"
    val authorize = "https://accounts.spotify.com/authorize"
    val search = root + "search"
    val artists = root + "artists"
    val albums = root + "albums"
    val playlists = root + "me/playlists"

    val data = Map(
      "redirect_uri" -> Seq(redirectUri),
      "grant_type" -> Seq("authorization_code"),
      "client_id" -> Seq(clientId),
      "client_secret" -> Seq(clientSecret)
    )
  }

  override def favouriteMusicRetrievalRequest(accessToken:String, page:String):Future[WSResponse] =
    WS.url(apiEndpoints.tracks)
      .withQueryString("limit" -> "50", "offset" -> page)
      .withHeaders("Authorization" -> s"Bearer $accessToken").get()

  override def getPageInformation(json: JsValue): (Boolean, Int) = {
    val offset = (json \ "offset").as[Int]
    val total = (json \ "total").as[Int]
    val limit = (json \ "limit").as[Int]
    val next = offset + limit
    (next < total, next)
  }

  override protected def playlistRequest(accessToken: String): Future[WSResponse] = {
    WS.url(apiEndpoints.playlists)
      .withQueryString("limit" -> Constants.maxPlaylistCountToImport)
      .withHeaders("Authorization" -> s"Bearer $accessToken").get()
  }

  override protected def getNextPage(trackJs: JsValue): (Boolean, String) = {
    (trackJs \ "next").asOpt[String] match {
      case Some(url) => (true,url)
      case _ => (false,"")
    }
  }

  override protected def trackListLinks(js: JsValue): List[String] = {
    val list = (js \ "items").as[List[JsValue]]
    list.map { item =>
      (item \ "href").as[String] + "/tracks"
    }
  }

  override protected def authenticateTrackRetrievalRequest(wsRequest: WSRequest, accessToken: String): WSRequest = {
    wsRequest.withHeaders("Authorization" -> s"Bearer $accessToken")
  }
}
