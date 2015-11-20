package models.service.library

import models.service.library.util.JsonConversion
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SoundcloudLibrary(identifier: Either[Int, String]) extends Library(identifier) with JsonConversion {

  def doJsonConversion(js: JsValue): Seq[Map[String, String]] = {
    val result = js.as[Seq[JsValue]]
    result map { entity =>
      /*
      val id = (entity \ "id").as[Int]
      val user = (entity \ "user").as[JsValue]
      val artist = (user \ "username").as[String]
      saveArtistsSoundcloudId(artist, id)
      TODO check if artist is a real artist and not some normal Soundcloud user, Musicbrainz could help
      IGNORE all others
      */
      Map("artist" -> "NOTYETSUPPORTED", "album" -> "WORKINPROGRESS")
    }
  }

  private def saveArtistsSoundcloudId(artist:String, id:Int) = {
    Future {
      pushToArtistIdQueue(artist, id.toString, "soundcloud")
    }
  }
}
