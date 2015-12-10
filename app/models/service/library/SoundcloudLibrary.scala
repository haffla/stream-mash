package models.service.library

import models.service.Constants
import models.service.api.discover.MusicBrainzApi
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SoundcloudLibrary(identifier: Either[Int, String]) extends Library(identifier) {

  def convertJsonToSeq(json: Option[JsValue]):Future[Seq[Map[String,String]]] = {
    json match {
      case Some(js) =>
        doJsonConversion(js)
      case None =>
        throw new Exception(Constants.userTracksRetrievalError)
    }
  }

  def doJsonConversion(js: JsValue): Future[Seq[Map[String, String]]] = {
    val result = js.as[Seq[JsValue]]
    Future.sequence {
      result map { entity =>
        Thread.sleep(1000)
        val isTrack = (entity \ "kind").as[String] == "track"

        val id = (entity \ "id").as[Int]
        val user = (entity \ "user").as[JsValue]
        val artist = (user \ "username").as[String]
        val title = (entity \ "title").as[String]

        for {
          musicBrainzResult <- MusicBrainzApi.findAlbumOfTrack(title, artist, 1100)
        } yield musicBrainzResult.headOption
      }
    } map(x => x filter(y => y.isDefined) map(z => z.get))
  }

  private def saveArtistsSoundcloudId(artist:String, id:Int) = {
    Future {
      pushToArtistIdQueue(artist, id.toString, "soundcloud")
    }
  }
}
