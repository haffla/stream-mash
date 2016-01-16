package models.service.api

import models.database.facade.{SpotifyArtistFacade, SpotifyFacade}
import models.service.oauth.SpotifyService.apiEndpoints
import models.util.Logging
import play.api.Play.current
import play.api.libs.json.{JsValue, JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse, WS}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SpotifyApiFacade extends ApiFacade {

  private def authenticateRequest(ws:WSRequest, token:String) = {
    ws.withHeaders("Authorization" -> s"Bearer $token")
  }

  override def getArtistId(artist:String, token:Option[String] = None, recordAbsence:Boolean = false):Future[Option[(String,String)]] = {
    val unAuthenticatedRequest = WS.url(apiEndpoints.search).withQueryString("type" -> "artist", "q" -> artist)
    val request = token match {
      case Some(tkn) => authenticateRequest(unAuthenticatedRequest, tkn)
      case _ => unAuthenticatedRequest
    }
    request.get().map {
      response =>
        response.status match {
          case 200 =>
            val json = Json.parse(response.body)
            val artists = (json \ "artists" \ "items").as[List[JsObject]]
            artists.headOption.map { head =>
              val id = (head \ "id").asOpt[String]
              id match {
                case Some(i) =>
                  SpotifyFacade.saveArtistWithServiceId(artist, i)
                  Some((artist, i))
                case None => None
              }
            }.getOrElse {
              if(recordAbsence) {
                // The artist does not exist on Spotify
              }
              None
            }

          case http_code =>
            logError(http_code, response.body)
            None
        }
    }
  }

  def getArtistInfoForFrontend(id:String):Future[JsValue] = {
    WS.url(apiEndpoints.artists + "/" + id).get().map { response =>
      response.status match {
        case 200 =>
          val js = Json.parse(response.body)
          SpotifyArtistFacade.saveInfoAboutArtist(js)
          js
        case http_code =>
          logError(http_code, response.body)
          Json.obj("error" -> true)
      }
    }
  }

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

  private def logError(code:Int, error:String) = {
    Logging.error(ich, "Error getting Spotify artist: " + code + "\n" + error)
  }
}
