package models.service.library

import models.database.facade.api.SpotifyFacade
import models.service.Constants
import models.service.library.util.JsonConversion
import play.api.libs.json.JsValue

class SpotifyLibrary(identifier: Either[Int, String]) extends Library(identifier, "spotify") with JsonConversion {

  def doJsonConversion(js: JsValue): Seq[Map[String, String]] = {
    val items = (js \ "items").as[Seq[JsValue]]
    val totalLength = items.length
    items.zipWithIndex.map { case (entity,i) =>
      val position = i + 1
      apiHelper.setRetrievalProcessProgress(position.toDouble / totalLength)
      val trackEntity = (entity \ "track").as[JsValue]
      val album = (trackEntity \ "album" \ "name").as[String]
      val artist = (trackEntity \ "artists").as[Seq[JsValue]].headOption match {
        case Some(art) =>
          val artist = (art \ "name").as[String]
          val id = (art \ "id").as[String]
          SpotifyFacade.saveArtistWithServiceId(artist,id)
          artist
        case None => Constants.mapKeyUnknownArtist
      }
      val track = (trackEntity \ "name").as[String]
      Map(
        Constants.mapKeyArtist -> artist,
        Constants.mapKeyAlbum -> album,
        Constants.mapKeyTrack -> track
      )
    }
  }
}