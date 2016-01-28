package models.service.library

import models.util.Constants
import play.api.libs.json.JsValue

object AudioFileLibrary {
  def apply(identifier: Either[Int, String]) = new AudioFileLibrary(identifier)
}

class AudioFileLibrary(identifier: Either[Int, String]) extends Library(identifier, "audio") {

  def handleFiles(files:JsValue):Seq[Map[String,String]] = {
    val jsList = files.as[List[JsValue]]
    jsList map { file =>
      val artist = (file \ "artist").as[String]
      val album = (file \ "album").as[String]
      val track = (file \ "title").as[String]
      Map(
        Constants.mapKeyArtist -> artist,
        Constants.mapKeyAlbum -> album,
        Constants.mapKeyTrack -> track
      )
    }
  }

  def process(files:JsValue):Unit = {
    convertSeqToMap(handleFiles(files))
    apiHelper.setRetrievalProcessDone()
  }
}
