package models.service.library

import models.service.Constants
import models.service.api.discover.{ApiHelper, MusicBrainzApi}
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SoundcloudLibrary(identifier: Either[Int, String]) extends Library(identifier) {

  val apiHelper = new ApiHelper("soundcloud", identifier)

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
    val totalLength = result.length
    Future.sequence {
      result.zipWithIndex.map { case (entity, i) =>
        val position = i + 1
        apiHelper.setRetrievalProcessProgress(position.toDouble / totalLength)
        Thread.sleep(1000)
        val isTrack = (entity \ "kind").as[String] == "track"
        if(isTrack) {
          val id = (entity \ "id").as[Int]
          val user = (entity \ "user").as[JsValue]
          val artist = (user \ "username").as[String]
          val title = (entity \ "title").as[String]
          val res = for {
            musicBrainzResult <- MusicBrainzApi.findAlbumOfTrack(title, artist)
          } yield musicBrainzResult.headOption
          saveArtistsSoundcloudId(res, id)
          res
        }
        else Future.successful(None)
      }
    } map(x => x filter(y => y.isDefined) map(z => z.get))
  }

  private def saveArtistsSoundcloudId(musicBrainzApiResponse:Future[Option[Map[String,String]]], id:Int) = {
    musicBrainzApiResponse.map {
        case Some(result) =>
          val artist = result(MusicBrainzApi.keyArtist)
          pushToArtistIdQueue(artist, id.toString, "soundcloud")
        case None => //do nothing
      }
  }
}
