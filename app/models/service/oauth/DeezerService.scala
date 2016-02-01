package models.service.oauth

import models.service.library.DeezerImporter
import models.service.oauth.DeezerService._
import models.service.util.ServiceAccessTokenHelper
import models.util.Constants
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeezerService(identifier:Either[Int,String]) extends ApiDataRequest(Constants.serviceDeezer, identifier) {

  override val serviceAccessTokenHelper: ServiceAccessTokenHelper = new ServiceAccessTokenHelper(Constants.serviceDeezer, identifier)
  val library = new DeezerImporter(identifier)

  override def doDataRequest(code: String): Future[(Option[String],Option[String])] = {
    val futureResponse: Future[WSResponse] = WS.url(apiEndpoints.token).withQueryString(
      "app_id" -> clientId,
      "secret" -> clientSecret,
      "code" -> code,
      "output" -> "json").get()

    for {
      token <- getAccessToken(futureResponse)
      response <- requestPlaylists(token._1)
      top <- requestUsersTracks(token._1)
      seq = library.convertJsonToSeq(response ++ top)
      res = library.convertSeqToMap(seq)
    } yield token
  }
}
object DeezerService extends OAuthStreamingService with PlayListRetrieval with FavouriteMusicRetrieval with OauthRouting {

  def apply(identifier:Either[Int,String]) = new DeezerService(identifier)

  override val clientIdKey = "deezer.app.id"
  override val clientSecretKey = "deezer.app.secret"

  override val redirectUriPath = "/deezer/callback"
  override val cookieKey = "deezer_auth_state"

  override lazy val redirectUri = "http://haffla.de"

  val queryString:Map[String,Seq[String]] = Map(
    "app_id" -> Seq(clientId),
    "redirect_uri" -> Seq(redirectUri),
    "perms" -> Seq("basic_access, listening_history")
  )

  object apiEndpoints {
    val root = "http://api.deezer.com/"
    val token = "https://connect.deezer.com/oauth/access_token.php"
    val authorize = "https://connect.deezer.com/oauth/auth.php"
    val tracks = root + "user/me/tracks"
    val albums = root + "album"
    val artists = root + "artist"
    val search = root + "search"
    val playlists = root + "user/me/playlists"
  }

  override def trackListLinks(js:JsValue):List[String] = {
    val list = (js \ "data").as[List[JsValue]]
    list.map { item =>
      (item \ "tracklist").as[String]
    }
  }

  override def getNextPage(trackJs: JsValue):(Boolean,String) = {
    (trackJs \ "next").asOpt[String] match {
      case Some(url) => (true, url)
      case _ => (false, "")
    }
  }

  override def favouriteMusicRetrievalRequest(accessToken: String, page:String): Future[WSResponse] =
    getRequest(accessToken, apiEndpoints.tracks)

  override protected def playlistRequest(accessToken: String): Future[WSResponse] = {
    getRequest(accessToken, apiEndpoints.playlists)
  }

  private def getRequest(accessToken: String, url:String) = {
    WS.url(url).withQueryString("access_token" -> accessToken).get()
  }

  override def getPageInformation(js:JsValue):(Boolean,Int) = {
    (false,0)
  }

  override def authorizeEndpoint: String = apiEndpoints.authorize
}
