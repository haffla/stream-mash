package models.service.api.refresh

import models.auth.MessageDigest
import models.service.Constants
import models.service.oauth.{SpotifyService, OAuthStreamingService}
import models.service.util.ServiceAccessTokenHelper
import play.api.libs.ws.WS
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SpotifyRefresh(identifier:Either[Int,String]) extends OAuthStreamingService {

  val clientIdKey = SpotifyService.clientIdKey
  val clientSecretKey = SpotifyService.clientSecretKey

  val serviceAccessTokenHelper = new ServiceAccessTokenHelper(Constants.serviceSpotify, identifier)

  private def getRequest(token:String) = {
    val data = Map("grant_type" -> Seq(Constants.jsonKeyRefreshToken), Constants.jsonKeyRefreshToken -> Seq(token))
    val baseEncodedCredentials = MessageDigest.encodeBase64(clientId + ":" + clientSecret)
    WS.url(SpotifyService.apiEndpoints.token)
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
