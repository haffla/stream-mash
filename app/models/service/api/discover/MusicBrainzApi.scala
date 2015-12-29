package models.service.api.discover

import play.api.libs.ws.WS
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object MusicBrainzApi {

  val limit = 5
  val root = "http://musicbrainz.org/ws/2/"
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
    val url = root + "recording/?query=" + query
    WS.url(url).get() map { response =>
      if(response.status == 200) {
        val res = scala.xml.parsing.XhtmlParser(scala.io.Source.fromString(response.body)) \ "recording-list" \ "recording" map { rec =>
          //val title = rec \ "title" text
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

  /**
   * Look for the artist on MusicBrainz and return the standard name if found
   */
  def isKnownArtist(artistSearch:String):Future[Option[String]] = {
    val a = removeSpecialCharsAndWhiteSpace(artistSearch)
    val query = s""""$a"&limit=1"""
    val url = root + "artist/?query=" + query
    WS.url(url).get() map { response =>
      if(response.status == 200) {
        (scala.xml.parsing.XhtmlParser(scala.io.Source.fromString(response.body)) \ "artist-list" \ "artist").headOption.map { art =>
          val score = (art \\ s"@{$extNs}score").text
          if(score.toInt > 90) {
            Some(art \ "name" text)
          }
          else None
        }.getOrElse(None)
      }
      else {
        println(url)
        println(response.status, response.body)
        None
      }

    }
  }



  def removeSpecialCharsAndWhiteSpace(s:String):String = s.replaceAll("[^\\p{L}0-9_\\- ]", "").replaceAll(" +", " ")
}
