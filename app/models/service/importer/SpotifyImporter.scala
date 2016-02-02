package models.service.importer

import models.database.facade.api.SpotifyFacade
import models.service.importer.util.JsonConversion
import models.util.Constants
import play.api.libs.json.JsValue

class SpotifyImporter(identifier: Either[Int, String]) extends Importer(identifier, Constants.serviceSpotify) with JsonConversion {

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
          val id = (art \ "id").asOpt[String] match {
            case Some(artistId) =>
              SpotifyFacade.saveArtistWithServiceId(artist,artistId)
            case _ =>
          }
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