package models.service.library

import models.service.Constants
import models.service.library.util.JsonConversion
import play.api.libs.json.JsValue

class DeezerLibrary(identifier: Either[Int, String]) extends Library(identifier) with JsonConversion {

  def doJsonConversion(js: JsValue): Seq[Map[String, String]] = {
    val data = (js \ "data").as[Seq[JsValue]]
    data map { item =>
      val album = (item \ "title").as[String]
      val artist = (item \ "artist" \ "name").as[String]
      val artistId = (item \ "artist" \ "id").as[Int]
      saveArtistsLastfmIds((artist, artistId.toString))
      Map(Constants.mapKeyArtist -> artist, Constants.mapKeyAlbum -> album)
    }
  }

  private def saveArtistsLastfmIds(artistTuple:(String,String)) = {
    val artist = artistTuple._1
    val id = artistTuple._2
    pushToArtistIdQueue(artist, id, "deezer")
  }
}
