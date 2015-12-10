package models.service.api.discover

import play.api.libs.ws.WS
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object MusicBrainzApi {

  val limit = 5
  val extNs = "http://musicbrainz.org/ns/ext#-2.0"
  val keyArtist = "artist"
  val keyAlbum = "album"

  /**
   * Search for title and artist to find out to what album the recording belongs to.
   * Only return pairs with a certain similarity score
   */
  def findAlbumOfTrack(titleSearch:String, artistSearch:String, minScore:Int = 80):Future[Seq[Map[String,String]]] = synchronized {
    val t = removeSpecialCharsAndWhiteSpace(titleSearch)
    val a = removeSpecialCharsAndWhiteSpace(artistSearch)
    val query = s""""$t" AND artist:$a&limit=$limit"""
    val url = "http://musicbrainz.org/ws/2/recording/?query=" + query
    WS.url(url).get() map { response =>
      if(response.status == 200) {
        val res = scala.xml.parsing.XhtmlParser(scala.io.Source.fromString(response.body)) \ "recording-list" \ "recording" map { rec =>
          val title = rec \ "title" text
          val album = rec \ "release-list" \ "release" \ "title" text
          val artist = rec \ "artist-credit" \ "name-credit" \ "artist" \ "name" text
          val score = (rec \\ s"@{$extNs}score").text

          if(score.toInt > minScore) {
            Some(Map(keyAlbum -> album, keyArtist -> artist))
          }
          else None
        } filter(_.isDefined)

        res map(_.get)
      }
      else {
        println(url)
        println(response.status, response.body)
        Nil
      }
    }
  }

  def removeSpecialCharsAndWhiteSpace(s:String):String = s.replaceAll("[^\\p{L}0-9_\\- ]", "").replaceAll(" +", " ")
}
