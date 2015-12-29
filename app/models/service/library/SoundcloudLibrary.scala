package models.service.library

import models.database.facade.SoundcloudFacade
import models.service.Constants
import models.service.api.discover.MusicBrainzApi
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SoundcloudLibrary(identifier: Either[Int, String]) extends Library(identifier, "soundcloud") {

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
        val isTrack = (entity \ "kind").as[String] == "track"
        if(isTrack) {
          Thread.sleep(1000)
          val id = (entity \ "id").as[Int]
          val user = (entity \ "user").as[JsValue]
          val artist = (user \ "username").as[String]
          //val title = (entity \ "title").as[String]
          val res = for {
            musicBrainzResult <- MusicBrainzApi.isKnownArtist(artist)
          } yield {
              musicBrainzResult match {
                case Some(art) =>
                  SoundcloudFacade.saveArtistWithServiceId(art, id.toString)
                  Some(Map(Constants.mapKeyArtist -> art, Constants.mapKeyAlbum -> Constants.mapKeyUnknownAlbum, Constants.mapKeyTrack -> "UNKNOWNTRACK"))
                case None => None
              }

            }
          res
        }
        else Future.successful(None)
      }
    } map(x => x filter(y => y.isDefined) map(z => z.get))
  }
}
