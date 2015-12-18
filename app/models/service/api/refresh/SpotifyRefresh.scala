package models.service.api.refresh

import models.auth.MessageDigest
import models.service.oauth.OAuthStreamingServiceAbstract
import models.service.util.ServiceAccessTokenCache
import play.api.libs.ws.WS
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SpotifyRefresh(identifier:Either[Int,String]) extends OAuthStreamingServiceAbstract {

  val clientIdKey = "spotify.client.id"
  val clientSecretKey = "spotify.client.secret"
  val cookieKey = null
  val redirectUriPath = null

  val serviceAccessTokenCache = new ServiceAccessTokenCache("spotify", identifier)

  private def getRequest(token:String) = {
    println("got some token: " + token)
    val data = Map("grant_type" -> Seq("refresh_token"), "refresh_token" -> Seq(token))
    val baseEncodedCredentials = MessageDigest.encodeBase64(clientId + ":" + clientSecret)
    WS.url("https://accounts.spotify.com/api/token")
      .withHeaders("Authorization" -> s"Basic $baseEncodedCredentials")
      .post(data)
  }

  def refreshToken(token:String):Future[String] = {
    println("refreshing")
    getAccessToken(getRequest(token)) map {
      case Some(tkn) =>
        identifier match {
          case Left(id) => serviceAccessTokenCache.setAccessToken(tkn)
          case Right(_) =>
        }
        tkn
      case None => throw new Exception("We did not receive a refresh token.")
    }
  }
}

object SpotifyRefresh {
  def apply(identifier:Either[Int,String]) = new SpotifyRefresh(identifier)
}
