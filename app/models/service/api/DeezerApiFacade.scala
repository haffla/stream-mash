package models.service.api

import java.net.URLEncoder

import models.database.alias.Artist
import models.database.facade.api.DeezerFacade
import models.database.facade.service.{DeezerArtistFacade, ServiceArtistTrait}
import models.service.oauth.DeezerService.apiEndpoints
import play.api.Play.current
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WS, WSRequest, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DeezerApiFacade extends ApiFacade {

  override val serviceName = "deezer"
  override val serviceArtistFacade: ServiceArtistTrait = DeezerArtistFacade
  override def artistInfoUrl(id: String): String = {
    apiEndpoints.artists + "/" + id
  }

  def authenticateRequest(ws:WSRequest, token:String):WSRequest = {
    ws.withQueryString("access_token" -> token)
  }

  def unAuthRequest(artist:String): WSRequest = {
    val a:String = URLEncoder.encode(artist, "UTF-8")
    WS.url(apiEndpoints.search + s"/artist?q=$a&output=json&strict=on")
  }

  override def getAlbumInfoForFrontend(id:String, usersTracks:List[String]):Future[JsValue] = {
    for {
      albumDetailResponse <- WS.url(apiEndpoints.albums + s"/$id").get()
    } yield handleAlbumInfoResponses(albumDetailResponse:WSResponse, usersTracks)
  }

  override def handleJsonIdSearchResponse(
                                   json: JsValue,
                                   artist:Artist,
                                   identifier:Option[Either[Int,String]],
                                   artistNotPresentCallback: (String, Option[Either[Int,String]]) => Option[(Long, String)]): Option[(Long, String)] = {
    val artists = (json \ "data").as[List[JsObject]]
    artists.headOption.map { head =>
      val id = (head \ "id").asOpt[Int]
      id match {
        case Some(i) =>
          DeezerFacade.saveArtistWithServiceId(artist.name, i.toString)
          Some((artist.id, i.toString))
        case None => None
      }
    }.getOrElse(artistNotPresentCallback(artist.name, identifier))
  }

  private def handleAlbumInfoResponses(albumDetailResponse: WSResponse, usersTracks:List[String]):JsValue = {
    if(albumDetailResponse.status == 200) {
      val js = Json.parse(albumDetailResponse.body)
      val image = (js \ "cover_big").as[String]
      val deezer = (js \ "link").as[String]
      val tracks = (js \ "tracks" \ "data").as[List[JsValue]].map { track =>
        val spotifyTrackUrl = (track \ "link").as[String]
        val trackName = (track \ "title").as[String]
        val userHasTrack = usersTracks.contains(trackName)
        Json.obj(
          "name" -> trackName,
          "url" -> spotifyTrackUrl,
          "inCollection" -> userHasTrack
        )
      }
      Json.obj(
        "images" -> Json.obj("picture_big" -> image),
        "url" -> deezer,
        "tracks" -> tracks
      )
    }
    else Json.obj(
          "error" -> true,
          "status" -> albumDetailResponse.status,
          "text" -> albumDetailResponse.statusText
        )
  }
}
