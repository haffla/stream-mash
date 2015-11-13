package models.service

import com.rabbitmq.client.MessageProperties
import database.facade.SpotifyFacade
import models.Config
import models.messaging.RabbitMQConnection
import models.util.Logging
import play.api.libs.json.{JsValue, JsObject, Json}
import play.api.libs.ws.{WS, WSResponse}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play.current

import scala.concurrent.Future

object SpotifyService extends StreamingServiceAbstract{

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

  def requestUserData(code:String): Future[Option[JsValue]] = {
    val data = apiEndpoints.data + ("code" -> Seq(code))
    val futureResponse: Future[WSResponse] = WS.url(apiEndpoints.token).post(data)
    for {
      token <- getAccessToken(futureResponse)
      response <- requestUsersTracks(token)
    } yield response
  }

  def pushToArtistIdQueue(name: String, id: String) = {
    val connection = RabbitMQConnection.getConnection()
    val channel = connection.createChannel()
    channel.queueDeclare(Config.rabbitMqQueue, true, false, false, null)
    val message = Json.toJson(Map("name" -> name, "id" -> id)).toString()
    channel.basicPublish("", Config.rabbitMqQueue, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes)
    channel.close()
    connection.close()
  }

  private def requestUsersTracks(token:Option[String]):Future[Option[JsValue]] = {
    token match {
      case Some(accessToken) =>
        WS.url(apiEndpoints.tracks).withHeaders("Authorization" -> s"Bearer $accessToken").get() map { response =>
          response.status match {
            case 200 =>
              val json = Json.parse(response.body)
              val items = (json \ "items").asOpt[List[JsObject]]
              items.get.foreach { item =>
                val artists = (item \ "track" \ "artists").asOpt[List[JsObject]]
                artists.get.foreach { artist =>
                  val name = (artist \ "name").as[String]
                  val id = (artist \ "id").as[String]
                  val artistType = (artist \ "type").as[String]
                  if (artistType == "artist") {
                    pushToArtistIdQueue(name, id)
                  }
                }
              }
              Some(json)
            case http_code =>
              Logging.error(ich, Constants.userTracksRetrievalError + ": " +  http_code + "\n" + response.body)
              None
          }
        }
      case None => throw new Exception (Constants.accessTokenRetrievalError)
    }

  }

  def getArtistId(artist:String):Future[Option[String]] = {
    WS.url(apiEndpoints.search).withQueryString("type" -> "artist", "q" -> artist).get().map {
      response =>
        response.status match {
          case 200 =>
            val json = Json.parse(response.body)
            val artists = (json \ "artists" \ "items").as[List[JsObject]]
            if(artists.nonEmpty) {
              val id = (artists.head \ "id").asOpt[String]
              SpotifyFacade.saveArtistId(artist, id.get)
              id
            }
            else None
          case http_code =>
            Logging.error(ich, "Error getting id for artist: " + http_code + "\n" + response.body)
            None
        }
    }
  }
}
