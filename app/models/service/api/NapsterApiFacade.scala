package models.service.api

import models.database.alias.Artist
import models.database.facade.api.NapsterFacade
import models.database.facade.service.{NapsterArtistFacade, ServiceArtistTrait}
import models.service.oauth.NapsterService
import models.service.oauth.NapsterService.apiEndpoints
import models.util.Constants
import play.api.Play
import play.api.Play.current
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WS, WSRequest, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object NapsterApiFacade extends ApiFacade {

  private val apiKey = Play.current.configuration.getString(NapsterService.clientIdKey) match {
    case Some(key) => key
    case None => throw new Exception("The Napster API only works authenticated")
  }

  override val serviceName = Constants.serviceNapster
  override val serviceArtistFacade:ServiceArtistTrait = NapsterArtistFacade

  override def artistInfoUrl(id: String): String = {
    apiEndpoints.artists + "/" + id + s"?apikey=$apiKey"
  }

  override def authenticateRequest(ws:WSRequest, token:String):WSRequest = {
    ws.withQueryString("apikey" -> token)
  }

  def unAuthRequest(artist:String): WSRequest = WS.url(apiEndpoints.search + s"?q=$artist&type=artist")

  def getAlbumInfoForFrontend(id:String, usersTracks:List[String]):Future[JsValue] = {
    for {
      albumDetailResponse <- authenticateRequest(WS.url(apiEndpoints.albums + s"/$id"), apiKey).get()
      albumImagesResponse <- authenticateRequest(WS.url(apiEndpoints.albums + s"/$id/images"), apiKey).get()
    } yield handleAlbumInfoResponses(albumDetailResponse, albumImagesResponse, usersTracks)
  }

  private def handleAlbumInfoResponses(albumDetailResponse: WSResponse,
                                       albumImagesResponse: WSResponse,
                                       usersTracks:List[String]):JsValue = {
    if(albumDetailResponse.status == 200) {
      val jsFromAlbumResponse = Json.parse(albumDetailResponse.body)
      val images = albumImagesResponse.status match {
        case 200 => Json.parse(albumImagesResponse.body).as[JsValue]
        case _ => (jsFromAlbumResponse \ "images").as[JsValue]
      }
      val tracks = (jsFromAlbumResponse \ "tracks").as[List[JsValue]].map { track =>
        val trackUrl = (track \ "sample").as[String]
        val trackName = (track \ "name").as[String]
        val userHasTrack = usersTracks.contains(trackName)
        Json.obj(
          "name" -> trackName,
          "inCollection" -> userHasTrack,
          "url" -> trackUrl
        )
      }
      Json.obj(
        "images" -> images,
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
    val artists = json.as[List[JsObject]]
    artists.headOption.map { head =>
      val id = (head \ "id").asOpt[String]
      id match {
        case Some(i) =>
          NapsterFacade.updateArtistsServiceId(artist.id, i)
          Some((artist.id, i))
        case None => None
      }
    }.getOrElse(artistNotPresentCallback(artist.id, identifier))
  }
}
