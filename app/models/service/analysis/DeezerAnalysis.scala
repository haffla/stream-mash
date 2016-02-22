package models.service.analysis

import models.database.alias.Artist
import models.database.facade.service.DeezerArtistFacade
import models.service.api.DeezerApiFacade
import models.service.oauth.DeezerService
import models.util.{Constants, Logging}
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WS, WSRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeezerAnalysis(identifier:Either[Int,String],
                     usersFavouriteArtists: List[Artist])
                     extends ServiceAnalysis(identifier, usersFavouriteArtists, Constants.serviceDeezer) {

  override val searchEndpoint = DeezerService.apiEndpoints.artists
  override val serviceArtistFacade = DeezerArtistFacade
  override val apiFacade = DeezerApiFacade

  protected def urlForRequest(artistId:String):String = searchEndpoint + "/" + artistId + "/albums?output=json"

  protected override def handleJsonResponse(jsResp:JsValue):List[(String,String)] = {
    (jsResp \ "data").asOpt[List[JsValue]] match {
      case Some(data) =>
        data.flatMap {
          item =>
            val albumName = (item \ "title").as[String]
            val id = (item \ "id").as[Int]
            val recordType = (item \ "record_type").as[String]
            if (recordType == "album" || recordType == "single") Some((albumName, id.toString))
            else None
        }
      case _ => Nil
    }

  }

  protected override def getServiceFieldFromArtist(artist: Artist): Option[String] = artist.deezerId

  protected override def getAuthenticatedRequest(url:String, accessToken:String):WSRequest = {
    WS.url(url).withQueryString("access_token" -> accessToken)
  }

  protected override def testAndGetAccessToken():Future[Option[String]] = {
    serviceAccessTokenHelper.getAccessToken match {
      case Some(accessTkn) =>
        val url = urlForRequest("27")
        getAuthenticatedRequest(url, accessTkn).get().flatMap {
          response =>
            if(response.status != 200) {
              Logging.debug(ich, "Testing the access token was not successful")
              Future.successful(None)
            }
            else {
              val js = Json.parse(response.body)
              (js \ "error").asOpt[JsValue] match {
                case Some(_) =>
                  Future.successful(None)
                case _ =>
                  Future.successful(Some(accessTkn))
              }
            }
        }
      case None =>
        serviceAccessTokenHelper.getAnyAccessToken match {
          case Some(tkn) =>
            serviceAccessTokenHelper.setAccessToken(tkn)
            testAndGetAccessToken()
          case None => Future.successful(None)
        }
    }
  }
}

object DeezerAnalysis extends ServiceAnalysisTrait {
  def apply(identifier:Either[Int,String], userFavouriteArtists: List[Artist])
              = new DeezerAnalysis(identifier, userFavouriteArtists)
}


