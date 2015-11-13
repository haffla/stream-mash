package models.service

import models.auth.MessageDigest
import models.util.Logging
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object RdioService extends StreamingServiceAbstract {

  val clientIdKey = "rdio.client.id"
  val clientSecretKey = "rdio.client.secret"

  val redirectUriPath = "/rdio/callback"
  override lazy val redirectUri = "http://localhost:9000/rdio/callback"
  val cookieKey = "rdio_auth_state"

  val queryString:Map[String,Seq[String]] = Map(
    "client_id" -> Seq(clientId),
    "redirect_uri" -> Seq(redirectUri),
    "response_type" -> Seq("code")
  )

  object jsonKeys {
    val artist = "artist"
    val name = "name"
    val album = "album"
    val typeArtist = "r"
    val typeAlbum = "a"
    val typeTrack = "t"
  }

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

  def convertJsonToSeq(json: Option[JsValue]):Future[Seq[Map[String,String]]] = {
    json match {
      case Some(x) =>
        Future {
          val res = (x \ "result").as[Seq[JsValue]]
          res map { entity =>
            val typ = (entity \ "type").as[String]
            typ match {
              case jsonKeys.typeAlbum =>
                val artist = (entity \ jsonKeys.artist).as[String]
                val album = (entity \ jsonKeys.name).as[String]
                Map(Constants.mapKeyArtist -> artist, Constants.mapKeyAlbum -> album)
              case jsonKeys.typeTrack =>
                val artist = (entity \ jsonKeys.artist).as[String]
                val album = (entity \ jsonKeys.album).as[String]
                Map(Constants.mapKeyArtist -> artist, Constants.mapKeyAlbum -> album)
              case jsonKeys.typeArtist =>
                val artist = (entity \ jsonKeys.name).as[String]
                Map(Constants.mapKeyArtist -> artist)
            }
          }
        }
      case None =>
        Future.failed(new Exception(Constants.userTracksRetrievalError))
     }
  }

  def requestUserData(code:String):Future[Option[JsValue]] = {
    val data = apiEndpoints.data + ("code" -> Seq(code))
    val clientIdAndSecret = clientId + ":" + clientSecret
    val encodedAuthorization = MessageDigest.encodeBase64(clientIdAndSecret)
    val futureResponse: Future[WSResponse] = WS.url(apiEndpoints.token)
      .withHeaders("Authorization" -> s"Basic $encodedAuthorization").post(data)
    for {
      token <- getAccessToken(futureResponse)
      response <- requestUsersTracks(token)
      result <- convertJsonToSeq(response)
    } yield response
  }

  private def requestUsersTracks(token:Option[String]):Future[Option[JsValue]] = {
    token match {
      case Some(access_token) =>
        val data = Map(Constants.jsonKeyAccessToken -> Seq(access_token), "method" -> Seq(apiEndpoints.getFavourites))
        WS.url(apiEndpoints.mainApi)
          .withHeaders("Authorization" -> s"Bearer $access_token")
          .post(data) map {
            response =>
              response.status match {
                case 200 =>
                  val json = Json.parse(response.body)
                  Some(json)
                case http_code =>
                  Logging.error(ich, Constants.userTracksRetrievalError + ": " +  http_code + "\n" + response.body)
                  None
              }
        }
      case None => throw new Exception(Constants.accessTokenRetrievalError)
    }
  }
}
