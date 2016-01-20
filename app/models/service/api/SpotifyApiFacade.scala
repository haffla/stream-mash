package models.service.api

import models.database.alias.Artist
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

  override def getAlbumInfoForFrontend(id:String, usersTracks:List[String]):Future[JsValue] = {
    for {
      albumDetailResponse <- WS.url(apiEndpoints.albums + s"/$id").get()
    } yield handleAlbumInfoResponse(albumDetailResponse:WSResponse, usersTracks)
  }

  private def handleAlbumInfoResponse(albumDetailResponse: WSResponse, usersTracks:List[String]):JsValue = {
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

  override def handleJsonIdSearchResponse(
                                 json: JsValue,
                                 artist:Artist,
                                 identifier:Option[Either[Int,String]],
                                 artistNotPresentCallback: (Long, Option[Either[Int,String]]) => Option[(Long, String)]): Option[(Long, String)] = {
    val artists = (json \ "artists" \ "items").as[List[JsObject]]
    artists.headOption.map { js =>
      val id = (js \ "id").asOpt[String]
      id match {
        case Some(i) =>
          // Spotify has good quality pics of artists so we safe them here.
          SpotifyArtistFacade.saveInfoAboutArtist(js)
          SpotifyFacade.updateArtistsServiceId(artist.id, i)
          Some((artist.id, i))
        case None => None
      }
    }.getOrElse(artistNotPresentCallback(artist.id, identifier)) //TODO pass id here
  }
}
