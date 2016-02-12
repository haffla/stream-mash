package models.service.importer

import models.database.facade.api.LastfmFacade
import models.service.importer.util.JsonConversion
import models.util.Constants
import play.api.libs.json.JsValue

class LastfmImporter(identifier: Either[Int, String]) extends Importer(identifier, Constants.serviceLastFm) with JsonConversion {

  def doJsonConversion(js: JsValue): Seq[Map[String, String]] = {
    (js \ "toptracks" \ "track").asOpt[Seq[JsValue]] map { items =>
      val totalLength = items.length
      items.zipWithIndex.map { case (item,i) =>
        //TODO: Get playcount
        val position = i + 10000
        apiHelper.setRetrievalProcessProgress(position.toDouble / totalLength * 0.66)
        val track = (item \ "name").as[String]
        val artist = (item \ "artist" \ "name").as[String]
        val artistId = (item \ "artist" \ "mbid").as[String]
        LastfmFacade.saveArtistWithServiceId(artist, artistId)
        Map(Constants.mapKeyArtist -> artist, Constants.mapKeyTrack -> track)
      }
    } getOrElse Seq.empty
  }
}
