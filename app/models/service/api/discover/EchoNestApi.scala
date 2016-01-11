package models.service.api.discover

import models.database.facade.ArtistFacade
import play.api.libs.json.{Json, JsValue}
import play.api.{PlayException, Play}
import play.api.libs.ws.WS
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import java.net.URLEncoder

object EchoNestApi {

  val root = "http://developer.echonest.com/api/v4"
  val apiKey =Play.current.configuration.getString("echonest.apikey") match {
    case Some(key) => key
    case _ => throw new PlayException(this.getClass.toString, "Did not find an API key for the EchoNest Api in the configuration")
  }

  def getArtistImage(artist:String):Future[Option[String]] = {
    val url = root + s"/artist/images?api_key=$apiKey&format=json&name=${URLEncoder.encode(artist.toLowerCase, "UTF-8")}"
    WS.url(url).get().map { res =>
      val json = Json.parse(res.body)
      val images = (json \ "response" \ "images").as[List[JsValue]]
      val filteredBySize = images.filter {image =>
        val imageIsSmall = (image \ "width").asOpt[Int] match {
          case Some(w) => w < 750
          case _ => false
        }
        imageIsSmall && !(image \ "url").as[String].contains("userserve-ak.last.fm")
      }
      val sortedBySize = filteredBySize.sortBy(image => (image \ "width").as[Int])
      val urls = sortedBySize map (img => (img \ "url").as[String])
      urls.headOption match {
        case Some(pic) =>
          ArtistFacade.setArtistPic(artist, pic)
          Some(pic)
        case _ => None
      }
    }
  }
}
