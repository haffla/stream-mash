package models.service.api

import models.database.facade.api.SpotifyFacade
import models.database.facade.service.{ServiceArtistTrait, SpotifyArtistFacade}
import models.service.oauth.SpotifyService.apiEndpoints
import play.api.Play.current
import play.api.libs.json.{JsValue, JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse, WS}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SpotifyApiFacade extends ApiFacade {

  override val serviceName = "spotify"
  override val serviceArtistFacade:ServiceArtistTrait = SpotifyArtistFacade

  override def artistInfoUrl(id: String): String = {
    apiEndpoints.artists + "/" + id
  }

  def authenticateRequest(ws:WSRequest, token:String):WSRequest = {
    ws.withHeaders("Authorization" -> s"Bearer $token")
  }

  def unAuthRequest(artist:String): WSRequest = WS.url(apiEndpoints.search).withQueryString("type" -> "artist", "q" -> artist)

  private def handleAlbumInfoResponses(albumDetailResponse: WSResponse, usersTracks:List[String]):JsValue = {
    if(albumDetailResponse.status == 200) {
      val js = Json.parse(albumDetailResponse.body)
      val images = (js \ "images").as[JsValue]
      val spotifyAlbumUrl = (js \ "external_urls" \ "spotify").as[String]
      val tracks = (js \ "tracks" \ "items").as[List[JsValue]].map { track =>
        val spotifyTrackUrl = (track \ "external_urls" \ "spotify").as[String]
        val trackName = (track \ "name").as[String]
        val userHasTrack = usersTracks.contains(trackName)
        Json.obj(
          "name" -> trackName,
          "url" -> spotifyTrackUrl,
          "inCollection" -> userHasTrack
        )
      }
      Json.obj(
        "images" -> images,
        "url" -> spotifyAlbumUrl,
        "tracks" -> tracks
      )
    }
    else Json.obj(
          "error" -> true,
          "status" -> albumDetailResponse.status,
          "text" -> albumDetailResponse.statusText
        )
  }

  def getAlbumInfoForFrontend(id:String, usersTracks:List[String]):Future[JsValue] = {
    for {
      albumDetailResponse <- WS.url(apiEndpoints.albums + s"/$id").get()
    } yield handleAlbumInfoResponses(albumDetailResponse:WSResponse, usersTracks)
  }

  override def handleJsonIdSearchResponse(
                                 json: JsValue,
                                 artist:String,
                                 identifier:Option[Either[Int,String]],
                                 artistNotPresentCallback: (String, Option[Either[Int,String]]) => Option[(String, String)]): Option[(String, String)] = {
    val artists = (json \ "artists" \ "items").as[List[JsObject]]
    artists.headOption.map { head =>
      val id = (head \ "id").asOpt[String]
      id match {
        case Some(i) =>
          SpotifyFacade.saveArtistWithServiceId(artist, i)
          Some((artist, i))
        case None => None
      }
    }.getOrElse(artistNotPresentCallback(artist, identifier))
  }
}
