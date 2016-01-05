package models.service.library

import models.database.facade.DeezerFacade
import models.service.Constants
import models.service.library.util.JsonConversion
import play.api.libs.json.JsValue

class DeezerLibrary(identifier: Either[Int, String]) extends Library(identifier) with JsonConversion {

  def doJsonConversion(js: JsValue): Seq[Map[String, String]] = {
    val data = (js \ "data").as[Seq[JsValue]]
    println(data)
    data map { item =>
      val track = (item \ "title").as[String]
      val album = (item \ "album" \ "title").as[String]
      val artist = (item \ "artist" \ "name").as[String]
      val artistId = (item \ "artist" \ "id").as[Int]
      DeezerFacade.saveArtistWithServiceId(artist,artistId.toString)
      Map(Constants.mapKeyArtist -> artist, Constants.mapKeyTrack -> track, Constants.mapKeyAlbum -> album)
    }
  }
}
