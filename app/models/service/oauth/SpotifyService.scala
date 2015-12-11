package models.service.oauth

import models.service.Constants
import models.service.api.discover.ApiHelper
import models.service.library.SpotifyLibrary
import models.service.oauth.SpotifyService.{apiEndpoints, _}
import models.util.Logging
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.Future

class SpotifyService(identifier: Either[Int, String]) extends ApiDataRequest("spotify", identifier) {

  val library = new SpotifyLibrary(identifier)

  def doDataRequest(code:String) = {
    val data = apiEndpoints.data + ("code" -> Seq(code))
    val futureResponse: Future[WSResponse] = WS.url(apiEndpoints.token).post(data)
    for {
      token <- getAccessToken(futureResponse)
      response <- requestUsersTracks(token)
      seq = library.convertJsonToSeq(response)
      result = library.convertSeqToMap(seq)
    } yield true
  }
}

object SpotifyService extends StreamingServiceAbstract {

  def apply(identifier: Either[Int, String]) = new SpotifyService(identifier)
  val clientIdKey = "spotify.client.id"
  val clientSecretKey = "spotify.client.secret"
  val redirectUriPath = "/spotify/callback"
  val scope:Seq[String] = Seq(
    "user-read-private",
    "playlist-read-private",
    "user-follow-read",
    "user-library-read"
  )
  val cookieKey = "spotify_auth_state"

  val queryString:Map[String,Seq[String]] = Map(
    "response_type" -> Seq("code"),
    "client_id" -> Seq(clientId),
    "scope" -> Seq(scope.mkString(" ")),
    "redirect_uri" -> Seq(redirectUri)
  )

  object apiEndpoints {
    val tracks = "https://api.spotify.com/v1/me/tracks"
    val token = "https://accounts.spotify.com/api/token"
    val authorize = "https://accounts.spotify.com/authorize"
    val search = "https://api.spotify.com/v1/search"

    val data = Map(
      "redirect_uri" -> Seq(redirectUri),
      "grant_type" -> Seq("authorization_code"),
      "client_id" -> Seq(clientId),
      "client_secret" -> Seq(clientSecret)
    )
  }

  private def requestUsersTracks(token:Option[String]):Future[Option[JsValue]] = {
    token match {
      case Some(accessToken) =>
        WS.url(apiEndpoints.tracks).withHeaders("Authorization" -> s"Bearer $accessToken").get() map { response =>
          response.status match {
            case 200 =>
              val json = Json.parse(response.body)
              Some(json)
            case http_code =>
              Logging.error(ich, Constants.userTracksRetrievalError + ": " +  http_code + "\n" + response.body)
              None
          }
        }
      case None => throw new Exception (Constants.accessTokenRetrievalError)
    }

  }
}
