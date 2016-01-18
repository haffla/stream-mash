package models.service.library

import models.database.facade.api.DeezerFacade
import models.service.Constants
import models.service.library.util.JsonConversion
import play.api.libs.json.JsValue

class DeezerLibrary(identifier: Either[Int, String]) extends Library(identifier, "deezer") with JsonConversion {

  def doJsonConversion(js: JsValue): Seq[Map[String, String]] = {
    val data = (js \ "data").as[Seq[JsValue]]
    data map { item =>
      val track = (item \ "title").as[String]
      val album = (item \ "album" \ "title").as[String]
      val artist = (item \ "artist" \ "name").as[String]
      val artistId = (item \ "artist" \ "id").as[Int]
      val artistPic = (item \ "artist" \ "picture_small").asOpt[String]
      DeezerFacade.saveArtistWithServiceId(artist,artistId.toString,artistPic)
      Map(Constants.mapKeyArtist -> artist, Constants.mapKeyTrack -> track, Constants.mapKeyAlbum -> album)
    }
  }
}
