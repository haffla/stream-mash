package controllers

import akka.actor.Status.Success
import com.google.common.io.BaseEncoding
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.ws._
import play.mvc.Http.Response
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play.current

class SpotifyController extends Controller {

  val CLIENT_ID = "9cc006e3d2c847a1834f7324af205581"
  val CLIENT_SECRET = "21086e7c74354aeba8d6b4e538768c12"
  val REDIRECT_URI = "http://localhost:9000/callback"
  val SCOPE = "user-read-private user-read-email"
  val POSSIBLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
  val SPOTIFY_COOKIE_KEY = "spotify_auth_state"

  object ApiEndpoints {
    val ME = "https://api.spotify.com/v1/me"
    val TOKEN = "https://accounts.spotify.com/api/token"

    val DATA = Map(
      "redirect_uri" -> Seq(REDIRECT_URI),
      "grant_type" -> Seq("authorization_code"),
      "client_id" -> Seq(CLIENT_ID),
      "client_secret" -> Seq(CLIENT_SECRET)
    )
  }

  val queryString:Map[String,Seq[String]] = Map(
    "response_type" -> Seq("code"),
    "client_id" -> Seq(CLIENT_ID),
    "scope" -> Seq(SCOPE),
    "redirect_uri" -> Seq(REDIRECT_URI)
  )

  def loginPage = Action { implicit request =>
    val state = generateRandomString(16)
    val withState = queryString + ("state" -> Seq(state))
    Redirect("https://accounts.spotify.com/authorize", withState)
      .withCookies(Cookie(SPOTIFY_COOKIE_KEY, state))
  }

  def generateRandomString(length:Int):String = {
    def buildString(s:String):String = {
      if(s.length >= length) s
      else {
        val rand = Math.random * (POSSIBLE.length - 1)
        buildString(s + POSSIBLE.charAt(rand.toInt))
      }
    }
    buildString("")
  }

  //Inspired by https://github.com/StarTrack/server
  def getTokens(futureReponse: Future[WSResponse]): Future[Option[(String, String)]] = {
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
              println("No tokens! " + response.body)
              None
          }
        case http_code =>
          println("Error: " + http_code + response.body)
          None
      }
    )
  }

  def callback = Action.async { implicit request =>
    val state = request.getQueryString("state").orNull
    val code = request.getQueryString("code").orNull
    val storedState = request.cookies.get(SPOTIFY_COOKIE_KEY).get.value
    if(state == null || state != storedState) {
      Future.successful(Ok("Error: State Mismatch"))
    } else {
      val data = ApiEndpoints.DATA + ("code" -> Seq(code))
      val futureResponse: Future[WSResponse] = WS.url(ApiEndpoints.TOKEN).post(data)

      val wsResponse:Future[Option[WSResponse]] = getTokens(futureResponse).flatMap(
        tokens => {
          val user = getSpotifyUser(tokens)
          user.flatMap(x => Future.successful(x))
        }
      )
      wsResponse.map {
        case Some(response) => Ok(Json.parse(response.bodyAsBytes))
        case None => Ok("")
      }
    }

  }

  def getSpotifyUser(tokens:Option[(String,String)]):Future[Option[WSResponse]] = {
      val access_token = tokens.get._1
      val refresh_token = tokens.get._2
      WS.url(ApiEndpoints.ME).withHeaders("Authorization" -> s"Bearer $access_token").get()
        .map(response =>
          response.status match {
            case 200 =>
              //println(response.body)
              Some(response)
            case http_code =>
              val error_message = "HTTP code: %d \nResponse: %s".format(http_code, response.body)
              println(error_message)
              None
          }
        )
  }
}