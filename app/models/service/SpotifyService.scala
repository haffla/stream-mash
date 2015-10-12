package models.service

import com.rabbitmq.client.MessageProperties
import models.Config
import models.messaging.RabbitMQConnection
import models.util.{SpotifyLibrary, Logging}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WS, WSResponse}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.PlayException
import play.api.Play
import play.api.Play.current

import scala.concurrent.Future

object SpotifyService {

  val ich = this.getClass.toString

  val client_id_key = "spotify.client.id"
  val client_secret_key = "spotify.client.secret"

  def confErrorTitle(s:String):String = {
    s"Spotify $s was not found."
  }
  def confErrorDescription(s:String, key:String):String = {
    s"Please provide a valid $s in the configuration file. Example: $key=[$s goes here]"
  }

  val CLIENT_ID = Play.current.configuration.getString(client_id_key) match {
    case Some(id) => id
    case None => throw new PlayException(confErrorTitle("client id"),confErrorDescription("client id", client_id_key))
  }
  val CLIENT_SECRET = Play.current.configuration.getString(client_secret_key) match {
    case Some(secret) => secret
    case None => throw new PlayException(confErrorTitle("client secret"), confErrorDescription("client secret", client_secret_key))
  }
  val REDIRECT_URI = "http://localhost:9000/callback"
  val SCOPE:Seq[String] = Seq(
    "user-read-private",
    "playlist-read-private",
    "user-follow-read",
    "user-library-read"
  )
  val SPOTIFY_COOKIE_KEY = "spotify_auth_state"

  val queryString:Map[String,Seq[String]] = Map(
    "response_type" -> Seq("code"),
    "client_id" -> Seq(CLIENT_ID),
    "scope" -> Seq(SCOPE.mkString(" ")),
    "redirect_uri" -> Seq(REDIRECT_URI)
  )

  object ApiEndpoints {
    val TRACKS = "https://api.spotify.com/v1/me/tracks"
    val TOKEN = "https://accounts.spotify.com/api/token"
    val AUTHORIZE = "https://accounts.spotify.com/authorize"
    val SEARCH = "https://api.spotify.com/v1/search"

    val DATA = Map(
      "redirect_uri" -> Seq(REDIRECT_URI),
      "grant_type" -> Seq("authorization_code"),
      "client_id" -> Seq(CLIENT_ID),
      "client_secret" -> Seq(CLIENT_SECRET)
    )
  }

  def requestAccessTokens(code:String): Future[Option[WSResponse]] = {
    val data = ApiEndpoints.DATA + ("code" -> Seq(code))
    val futureResponse: Future[WSResponse] = WS.url(ApiEndpoints.TOKEN).post(data)
    for {
      tokens <- getTokens(futureResponse)
      response <- requestUsersTracks(tokens)
    } yield response
  }

  def pushToArtistIdQueue(name: String, id: String) = {
    val connection = RabbitMQConnection.getConnection()
    val channel = connection.createChannel()
    channel.queueDeclare(Config.RABBITMQ_QUEUE, true, false, false, null)
    val message = s"$name|%|$id"
    channel.basicPublish("", Config.RABBITMQ_QUEUE, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes)
    channel.close()
    connection.close()
  }

  private def requestUsersTracks(tokens:Option[(String,String)]):Future[Option[WSResponse]] = {
    val access_token = tokens.get._1
    val refresh_token = tokens.get._2
    WS.url(ApiEndpoints.TRACKS).withHeaders("Authorization" -> s"Bearer $access_token").get()
      .map(response =>
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
              if(artistType == "artist") {
                pushToArtistIdQueue(name, id)
              }
            }
          }
          Some(response)
        case http_code =>
          Logging.error(ich, "Error requesting user data: " + http_code + "\n" + response.body)
          None
      }
      )
  }

  //Inspired by https://github.com/StarTrack/server
  private def getTokens(futureReponse: Future[WSResponse]): Future[Option[(String, String)]] = {
    futureReponse.map(response =>
      response.status match {
        case 200 =>
          val json = Json.parse(response.body)
          //val token_type = (json \ "token_type").asOpt[String]
          val refresh_token = (json \ "refresh_token").asOpt[String]
          val access_token = (json \ "access_token").asOpt[String]
          (access_token, refresh_token) match {
            case (Some(access_tkn), Some(refresh_tkn)) =>
              Some((access_tkn,refresh_tkn))
            case _ =>
              Logging.error(ich, response.body)
              None
          }
        case http_code =>
          Logging.error(ich, "Error getting tokens: " + http_code + "\n" + response.body)
          None
      }
    )
  }

  def getArtistId(artist:String):Future[Option[String]] = {
    WS.url(ApiEndpoints.SEARCH).withQueryString("type" -> "artist", "q" -> artist).get().map {
      response =>
        response.status match {
          case 200 =>
            val json = Json.parse(response.body)
            val artists = (json \ "artists" \ "items").as[List[JsObject]]
            if(artists.nonEmpty) {
              val id = (artists.head \ "id").asOpt[String]
              SpotifyLibrary.saveArtistId(artist, id.get)
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
