package models.service.library

import models.database.facade.LastfmFacade
import models.service.Constants
import models.service.library.util.JsonConversion
import play.api.libs.json.JsValue

class LastfmLibrary(identifier: Either[Int, String]) extends Library(identifier) with JsonConversion {

  def doJsonConversion(js: JsValue): Seq[Map[String, String]] = {
    (js \ "topalbums" \ "album").asOpt[Seq[JsValue]] map { items =>
      items map { item =>
        val album = (item \ "name").as[String]
        val artist = (item \ "artist" \ "name").as[String]
        val artistId = (item \ "artist" \ "mbid").as[String]
        LastfmFacade.saveArtistWithServiceId(artist, artistId)
        Map(Constants.mapKeyArtist -> artist, Constants.mapKeyAlbum -> album)
      }
    } getOrElse Seq.empty
  }
}
