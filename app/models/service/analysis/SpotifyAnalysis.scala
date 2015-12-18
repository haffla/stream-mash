package models.service.analysis

import models.database.facade.{ArtistFacade, AlbumFacade}
import models.service.api.SpotifyApiFacade
import models.service.util.ServiceAccessTokenCache
import models.util.Logging
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.WS
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SpotifyAnalysis(identifier:Either[Int,String]) extends ServiceAnalysis(identifier, "spotify") {

  val token:Option[String] = serviceAccessTokenCache.getAccessToken

  val searchEndpoint = "https://api.spotify.com/v1/artists/"

  val albumFacade = AlbumFacade(identifier)
  val artistFacade = ArtistFacade(identifier)

  def analyse():Future[JsValue] = {
    for {
      albums <- albumFacade.getUsersAlbumCollection
      ids <- getIds(albums)
      artists <- getAlbumsOfArtists(ids)
    } yield artists.head._2
  }

  def getIds(albums: Option[Map[String, Set[String]]]):Future[Set[Option[(String,String)]]] = {
    albums match {
      case Some(albs) =>
        val artists = albs.keySet
        Future.sequence {
          artists.map { artist =>
            artistFacade.getArtistByName(artist) flatMap {
              case Some(art) =>
                art.spotifyId match {
                  case Some(id) => Future.successful(Some((artist, id)))
                  case None => SpotifyApiFacade.getArtistId(artist)
                }
              case None => SpotifyApiFacade.getArtistId(artist)
            }
          }
        }
      case None => Future.successful(Set())
    }
  }

  def getAlbumsOfArtists(ids: Set[Option[(String,String)]]):Future[Set[(String,JsValue)]] = {
    val searchList:Set[(String,String)] = ids.filter(_.isDefined).map(_.get)
    Future.sequence {
      searchList map { artist =>
        val artistName = artist._1
        val artistId = artist._2
        val url = searchEndpoint + artistId + "/albums?market=DE&album_type=album"
        token match {
          case Some(t) =>
            WS.url(url).withHeaders("Authorization" -> s"Bearer $t").get() map { response =>
              if(response.status != 200) {
                Logging.debug(this.getClass.toString, response.body.toString)
              }
              (artistName, Json.parse(response.body))
            }
          case None => Future.failed(new Exception("An access token could not be found"))
        }
      }
    }
  }

  def processResponses(responses: Future[Set[(String,JsValue)]]) = {
    responses map { jsSet =>
      jsSet.foreach { jsTuple =>
        processSingleResponse(jsTuple)
      }
    }
  }

  def processSingleResponse(jsTuple:(String,JsValue)) = {
    val artist = jsTuple._1
    val js = jsTuple._2
    val items = (js \ "items").as[JsValue]
    
  }
}

object SpotifyAnalysis {
  def apply(identifier:Either[Int,String]) = new SpotifyAnalysis(identifier)
}
