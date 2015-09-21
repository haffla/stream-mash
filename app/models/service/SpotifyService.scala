package models.service

import models.util.Logging
import play.api.libs.json.Json
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
    "playlist-read-collaborative",
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
    val ME = "https://api.spotify.com/v1/me"
    val TOKEN = "https://accounts.spotify.com/api/token"
    val AUTHORIZE = "https://accounts.spotify.com/authorize"

    val DATA = Map(
      "redirect_uri" -> Seq(REDIRECT_URI),
      "grant_type" -> Seq("authorization_code"),
      "client_id" -> Seq(CLIENT_ID),
      "client_secret" -> Seq(CLIENT_SECRET)
    )
  }

  def requestAuthorization(code:String): Future[Option[WSResponse]] = {
    val data = ApiEndpoints.DATA + ("code" -> Seq(code))
    val futureResponse: Future[WSResponse] = WS.url(ApiEndpoints.TOKEN).post(data)
    for {
      tokens <- getTokens(futureResponse)
      response <- requestUserData(tokens)
    } yield response
  }

  private def requestUserData(tokens:Option[(String,String)]):Future[Option[WSResponse]] = {
    val access_token = tokens.get._1
    val refresh_token = tokens.get._2
    WS.url(ApiEndpoints.ME).withHeaders("Authorization" -> s"Bearer $access_token").get()
      .map(response =>
      response.status match {
        case 200 =>
          Some(response)
        case http_code =>
          val error_message = "HTTP code: %d \nResponse: %s".format(http_code, response.body)
          Logging.error(ich, error_message)
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
          Logging.error(ich, "Error: " + http_code + "\n" + response.body)
          None
      }
    )
  }
}
