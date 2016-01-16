package models.service.api.refresh

import models.auth.MessageDigest
import models.service.oauth.OAuthStreamingServiceAbstract
import models.service.util.ServiceAccessTokenHelper
import play.api.libs.ws.WS
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SpotifyRefresh(identifier:Either[Int,String]) extends OAuthStreamingServiceAbstract {

  val clientIdKey = "spotify.client.id"
  val clientSecretKey = "spotify.client.secret"

  val serviceAccessTokenHelper = new ServiceAccessTokenHelper("spotify", identifier)

  private def getRequest(token:String) = {
    val data = Map("grant_type" -> Seq("refresh_token"), "refresh_token" -> Seq(token))
    val baseEncodedCredentials = MessageDigest.encodeBase64(clientId + ":" + clientSecret)
    WS.url("https://accounts.spotify.com/api/token")
      .withHeaders("Authorization" -> s"Basic $baseEncodedCredentials").post(data)
  }

  def refreshToken():Future[Option[String]] = {
    serviceAccessTokenHelper.getRefreshToken match {
      case Some(refreshTkn) =>
        getAccessToken(getRequest(refreshTkn)) map { tokens =>
          val (accessToken,refreshToken) = tokens
          accessToken match {
            case Some(accTkn) =>
              serviceAccessTokenHelper.setAccessToken(accTkn,refreshToken)
              Some(accTkn)
            case None =>
              throw new Exception("We did not receive a refresh token from Spotify.")
              None
          }
        }
      case _ => Future.successful(None)
    }

  }
}

object SpotifyRefresh {
  def apply(identifier:Either[Int,String]) = new SpotifyRefresh(identifier)
}
