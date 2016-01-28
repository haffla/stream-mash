package models.service.library

import models.util.Constants
import play.api.libs.json.JsValue

object AudioJsonImporter {
  def apply(identifier: Either[Int, String]) = new AudioJsonImporter(identifier)
}

class AudioJsonImporter(identifier: Either[Int, String]) extends Library(identifier, "audio") {

  def handleFiles(files:JsValue):Seq[Map[String,String]] = {
    val jsList = files.as[List[JsValue]]
    jsList.map { file =>
      val artist = (file \ "artist").asOpt[String]
      val album = (file \ "album").asOpt[String]
      val track = (file \ "title").asOpt[String]
      (artist,album,track) match {
        case (Some(art),Some(alb),Some(tr)) =>
          Map(
            Constants.mapKeyArtist -> art,
            Constants.mapKeyAlbum -> alb,
            Constants.mapKeyTrack -> tr
          )
        case _ => Map[String,String]()
      }
    }.filterNot(_.isEmpty)
  }

  def process(files:JsValue):Unit = {
    convertSeqToMap(handleFiles(files))
    apiHelper.setRetrievalProcessDone()
  }
}
