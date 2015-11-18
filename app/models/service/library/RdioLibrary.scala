package models.service.library

import models.service.Constants
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RdioLibrary(identifier: Either[Int, String]) extends Library(identifier) {

  val keyArtist = "artist"
  val keyName = "name"
  val keyAlbum = "album"
  val keyArtistKey = "artistKey"
  val typeArtist = "r"
  val typeAlbum = "a"
  val typeTrack = "t"

  def convertJsonToSeq(json: Option[JsValue]):Seq[Map[String,String]] = {
    json match {
      case Some(js) =>
        val res = (js \ "result").as[Seq[JsValue]]
        res map { entity =>
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
      case None =>
        throw new Exception(Constants.userTracksRetrievalError)
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
