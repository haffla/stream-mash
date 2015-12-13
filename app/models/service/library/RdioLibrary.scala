package models.service.library

import models.service.Constants
import models.service.library.util.JsonConversion
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RdioLibrary(identifier: Either[Int, String]) extends Library(identifier) with JsonConversion {

  val keyArtist = "artist"
  val keyName = "name"
  val keyAlbum = "album"
  val keyArtistKey = "artistKey"
  val typeArtist = "r"
  val typeAlbum = "a"
  val typeTrack = "t"

  def doJsonConversion(js: JsValue): Seq[Map[String, String]] = {
    val res = (js \ "result").as[Seq[JsValue]]
    val totalLength = res.length
    res.zipWithIndex.map { case (entity,i) =>
      val position = i + 1
      apiHelper.setRetrievalProcessProgress(position.toDouble / totalLength)
      val typ = (entity \ "type").as[String]
      val data:(String,String,String) = typ match {
        case `typeAlbum` =>
          val artist = (entity \ keyArtist).as[String]
          val album = (entity \ keyName).as[String]
          val rdioKey = (entity \ keyArtistKey).as[String]
          (artist,rdioKey,album)
        case `typeTrack` =>
          val artist = (entity \ keyArtist).as[String]
          val album = (entity \ keyAlbum).as[String]
          val rdioKey = (entity \ keyArtistKey).as[String]
          (artist,rdioKey,album)
        /*
         * Entities that do not have album will be ignored in convertSeqToMap method
         * This is just for saving the rdio key anyway.
         */
        case `typeArtist` =>
          val artist = (entity \ keyName).as[String]
          val rdioKey = (entity \ "key").as[String]
          (artist,rdioKey,"")
        case _ =>
          throw new UnsupportedOperationException("The JSON key for 'type' has not been defined.")
      }
      saveArtistsRdioIds(data)
      data match {
        case (art,key,alb) =>
          if(alb.nonEmpty) {
            Map(Constants.mapKeyArtist -> art, Constants.mapKeyAlbum -> alb, Constants.mapKeyRdioArtistId -> key)
          }
          else {
            Map(Constants.mapKeyArtist -> art, Constants.mapKeyRdioArtistId -> key)
          }
      }
    }
  }

  private def saveArtistsRdioIds(artistTuple:(String,String,String)) = {
    Future {
      val artist = artistTuple._1
      val id = artistTuple._2
      pushToArtistIdQueue(artist, id, "rdio")
    }
  }
}
