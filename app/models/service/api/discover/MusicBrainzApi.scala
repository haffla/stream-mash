package models.service.api.discover

import play.api.libs.ws.WS
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

import org.apache.commons.lang3.StringUtils

object MusicBrainzApi {

  val limit = 5
  val minScore = 75
  val extNs = "http://musicbrainz.org/ns/ext#-2.0"

  /**
   * Search for title and artist to find out to what album the recording belongs to.
   * Only return pairs with a certain similarity score
   */
  def lookForArtistRecording(titleSearch:String, artistSearch:String, score:Int = minScore):Future[Seq[Map[String,String]]] = {
    val query = s""""$titleSearch" AND artist:$artistSearch&limit=$limit"""
    val url = "http://musicbrainz.org/ws/2/recording/?query=" + query
    WS.url(url).get() map { response =>
      val res = scala.xml.parsing.XhtmlParser(scala.io.Source.fromString(response.body)) \ "recording-list" \ "recording" map { rec =>
        val title = rec \ "title" text
        val album = rec \ "release-list" \ "release" \ "title" text
        val artist = rec \ "artist-credit" \ "name-credit" \ "artist" \ "name" text
        val score = rec \\ s"@{$extNs}score" text
        
        if(score.toInt > minScore) {
          Some(Map("album" -> album, "artist" -> artist))
        }
        else None
      } filter(_.isDefined)

      res map(_.get)
    }
  }

  def areSimilar(s:String, t:String) = {
    StringUtils.getLevenshteinDistance(s.toLowerCase,t.toLowerCase) < 20
  }
}
