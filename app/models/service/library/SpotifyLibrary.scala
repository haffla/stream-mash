package models.service.library

import models.service.Constants
import models.service.library.util.JsonConversion
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SpotifyLibrary(identifier: Either[Int, String]) extends Library(identifier, "spotify") with JsonConversion {

  def doJsonConversion(js: JsValue): Seq[Map[String, String]] = {
    val items = (js \ "items").as[Seq[JsValue]]
    val totalLength = items.length
    items.zipWithIndex.map { case (entity,i) =>
      val position = i + 1
      apiHelper.setRetrievalProcessProgress(position.toDouble / totalLength)
      val track = (entity \ "track").asOpt[JsValue]
      track match {
        case Some(trackJson) =>
          val album = (trackJson \ "album" \ "name").as[String]
          val artists = (trackJson \ "artists").as[Seq[JsValue]]
          saveArtistsSpotifyIds(artists)
          val artist = (artists.head \ "name").as[String]
          Map(Constants.mapKeyArtist -> artist, Constants.mapKeyAlbum -> album)
        case None => throw new Exception("Missing key 'track' in JSON")
      }
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