package models.service.api

import models.database.facade.{SpotifyArtistFacade, SpotifyFacade}
import models.service.oauth.SpotifyService.apiEndpoints
import models.util.Logging
import play.api.Play.current
import play.api.libs.json.{JsValue, JsObject, Json}
import play.api.libs.ws.WS

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SpotifyApiFacade extends ApiFacade {

  def getArtistId(artist:String):Future[Option[(String,String)]] = {
    WS.url(apiEndpoints.search).withQueryString("type" -> "artist", "q" -> artist).get().map {
      response =>
        response.status match {
          case 200 =>
            val json = Json.parse(response.body)
            val artists = (json \ "artists" \ "items").as[List[JsObject]]
            artists.headOption.map { head =>
              val id = (head \ "id").asOpt[String]
              id match {
                case Some(i) =>
                  SpotifyFacade.saveArtistWithServiceId(artist, i)
                  Some((artist, i))
                case None => None
              }
            }.getOrElse(None)

          case http_code =>
            logError(http_code, response.body)
            None
        }
    }
  }

  def getArtistInfoForFrontend(id:String):Future[JsValue] = {
    WS.url(apiEndpoints.artists + "/" + id).get().map { response =>
      response.status match {
        case 200 =>
          val js = Json.parse(response.body)
          SpotifyArtistFacade.saveInfoAboutArtist(js)
          js
        case http_code =>
          logError(http_code, response.body)
          Json.obj("error" -> true)
      }
    }
  }

  private def logError(code:Int, error:String) = {
    Logging.error(ich, "Error getting Spotify artist: " + code + "\n" + error)
  }
}
