package models.service.library

import models.service.Constants
import play.api.libs.json.JsValue

class RdioLibrary(userId:Int) extends Library(userId) {

  val keyArtist = "artist"
  val keyName = "name"
  val keyAlbum = "album"
  val typeArtist = "r"
  val typeAlbum = "a"
  val typeTrack = "t"

  def convertJsonToSeq(json: Option[JsValue]):Seq[Map[String,String]] = {
    json match {
      case Some(js) =>
        val res = (js \ "result").as[Seq[JsValue]]
        res map { entity =>
          val typ = (entity \ "type").as[String]
          typ match {
            case typeAlbum =>
              val artist = (entity \ keyArtist).as[String]
              val album = (entity \ keyName).as[String]
              Map(Constants.mapKeyArtist -> artist, Constants.mapKeyAlbum -> album)
            case typeTrack =>
              val artist = (entity \ keyArtist).as[String]
              val album = (entity \ keyAlbum).as[String]
              Map(Constants.mapKeyArtist -> artist, Constants.mapKeyAlbum -> album)
            case typeArtist =>
              val artist = (entity \ keyName).as[String]
              Map(Constants.mapKeyArtist -> artist)
          }
        }
      case None =>
        throw new Exception(Constants.userTracksRetrievalError)
    }
  }
}
