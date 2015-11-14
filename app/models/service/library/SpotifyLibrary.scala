package models.service.library

import com.rabbitmq.client.MessageProperties
import models.Config
import models.messaging.RabbitMQConnection
import models.service.Constants
import play.api.libs.json.{Json, JsValue}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SpotifyLibrary(userId:Int) extends Library(userId) {

  def convertJsonToSeq(json: Option[JsValue]):Seq[Map[String,String]] = {
    json match {
      case Some(js) =>
        val items = (js \ "items").as[Seq[JsValue]]
        items.map { entity =>
          val track = (entity \ "track").asOpt[JsValue]
          track match {
            case Some(trackJson) =>
              val album = (trackJson \ "album" \ "name").as[String]
              val artists = (trackJson \ "artists").as[Seq[JsValue]]
              saveArtistsSpotifyIds(artists)
              val artist = (artists.head \ "name").as[String]
              Map(Constants.mapKeyArtist -> artist, Constants.mapKeyAlbum -> album)
          }
        }
      case None =>
        throw new Exception(Constants.userTracksRetrievalError)
    }
  }

  private def saveArtistsSpotifyIds(artists:Seq[JsValue]) = {
    Future {
      artists.foreach { artist =>
        val name = (artist \ "name").as[String]
        val id = (artist \ "id").as[String]
        val artistType = (artist \ "type").as[String]
        if (artistType == "artist") {
          pushToArtistIdQueue(name, id, "spotify")
        }
      }
    }
  }
}