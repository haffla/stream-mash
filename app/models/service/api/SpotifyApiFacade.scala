package models.service.api

import models.database.facade.SpotifyFacade
import models.service.oauth.SpotifyService.apiEndpoints
import models.util.Logging
import play.api.Play.current
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WS

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SpotifyApiFacade extends ApiFacade {

  val typ = "spotify"

  def getArtistId(artist:String):Future[Option[String]] = {
    WS.url(apiEndpoints.search).withQueryString("type" -> "artist", "q" -> artist).get().map {
      response =>
        response.status match {
          case 200 =>
            val json = Json.parse(response.body)
            val artists = (json \ "artists" \ "items").as[List[JsObject]]
            if(artists.nonEmpty) {
              val id = (artists.head \ "id").asOpt[String]
              pushToArtistIdQueue(artist, id.get)
              id
            }
            else None

          case http_code =>
            Logging.error(ich, "Error getting ID for artist from Spotify: " + http_code + "\n" + response.body)
            None
        }
    }
  }
}
