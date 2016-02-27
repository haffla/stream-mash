package models.service.api.discover

import models.util.TextWrangler
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
   * Look for the artist on MusicBrainz and return the standard name if found
   */
  def isKnownArtist(artistSearch:String):Future[Option[String]] = {
    val a = TextWrangler.cleanupString(artistSearch)
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
      else if(response.status == 503) {
        Some("unavailable")
      }
      else {
        println(url)
        println(response.status, response.body)
        None
      }

    }
  }
}
