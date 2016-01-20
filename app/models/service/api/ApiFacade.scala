package models.service.api

import models.database.alias.Artist
import models.database.facade.ServiceArtistAbsenceFacade
import models.database.facade.service.ServiceArtistTrait
import models.util.Logging
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WS, WSRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ApiFacade {

  lazy val ich = this.getClass.toString
  val serviceArtistFacade:ServiceArtistTrait
  val serviceName:String
  def unAuthRequest(artist:String): WSRequest
  def authenticateRequest(ws:WSRequest, token:String):WSRequest
  def artistInfoUrl(id:String):String
  def handleJsonIdSearchResponse(
                        json: JsValue,
                        artist:Artist,
                        identifier:Option[Either[Int,String]],
                        artistNotPresentCallback: (Long, Option[Either[Int,String]]) => Option[(Long, String)]): Option[(Long,String)]

  def getAlbumInfoForFrontend(id:String, usersTracks:List[String]):Future[JsValue]

  def artistNotPresentCallback(artistId:Long, identifier:Option[Either[Int,String]]):Option[(Long,String)] = {
    identifier match {
      case Some(id) => ServiceArtistAbsenceFacade(id).insertIfNotExists(artistId, serviceName)
      case None =>
    }
    None
  }

  def getArtistId(
      artist:Artist,
      token:Option[String] = None,
      identifier:Option[Either[Int,String]] = None):Future[Option[(Long,String)]] = {
    val unAuthenticatedRequest = unAuthRequest(artist.name)
    // In case a token is supplied as argument, authenticate the request with that token
    val request = token match {
      case Some(tkn) => authenticateRequest(unAuthenticatedRequest, tkn)
      case _ => unAuthenticatedRequest
    }
    request.get().map {
      response =>
        response.status match {
          case 200 =>
            val json = Json.parse(response.body)
            handleJsonIdSearchResponse(json, artist, identifier, artistNotPresentCallback)
          case http_code =>
            logError(http_code, response.body)
            None
        }
    }
  }

  def getArtistInfoForFrontend(id:String):Future[JsValue] = {
    WS.url(artistInfoUrl(id)).get().map { response =>
      response.status match {
        case 200 =>
          val js = Json.parse(response.body)
          serviceArtistFacade.saveInfoAboutArtist(js)
          js
        case http_code =>
          logError(http_code, response.body)
          Json.obj("error" -> true)
      }
    }
  }

  def logError(code:Int, error:String) = {
    Logging.error(ich, "Error getting " + serviceName + " artist: " + code + "\n" + error)
  }
}
