package models.service.library

import models.database.facade.api.NapsterFacade
import models.service.library.util.JsonConversion
import models.util.Constants
import play.api.libs.json.JsValue

class NapsterLibrary(identifier: Either[Int, String]) extends Library(identifier, Constants.serviceNapster) with JsonConversion {

  def doJsonConversion(js: JsValue): Seq[Map[String, String]] = {
    val items = js.as[Seq[JsValue]]
    val totalLength = items.length
    items.zipWithIndex.map { case (entity,i) =>
      val position = i + 1
      apiHelper.setRetrievalProcessProgress(position.toDouble / totalLength)
      val track = (entity \ "name").as[String]
      val artist = (entity \ "artist" \ "name").as[String]
      val artistId = (entity \ "artist" \ "id").as[String]
      val album = (entity \ "album" \ "name").as[String]
      NapsterFacade.saveArtistWithServiceId(artist, artistId)
      Map(
        Constants.mapKeyArtist -> artist,
        Constants.mapKeyAlbum -> album,
        Constants.mapKeyTrack -> track
      )
    }
  }
}