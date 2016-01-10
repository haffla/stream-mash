package models.service.library

import models.database.facade.SpotifyFacade
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
      val artists = (trackEntity \ "artists").as[Seq[JsValue]]
      val artist = (artists.head \ "name").as[String]
      val id = (artists.head \ "id").as[String]
      val track = (trackEntity \ "name").as[String]
      SpotifyFacade.saveArtistWithServiceId(artist, id)
      Map(
        Constants.mapKeyArtist -> artist,
        Constants.mapKeyAlbum -> album,
        Constants.mapKeyTrack -> track
      )
    }
  }
}