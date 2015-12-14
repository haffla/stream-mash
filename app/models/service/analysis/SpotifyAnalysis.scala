package models.service.analysis

import models.database.facade.{ArtistFacade, AlbumFacade}
import models.service.api.SpotifyApiFacade
import models.service.util.ServiceAccessTokenCache
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.WS
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SpotifyAnalysis(identifier:Either[Int,String]) {

  val serviceAccessTokenCache = new ServiceAccessTokenCache("spotify", identifier)
  val token:Option[String] = serviceAccessTokenCache.getAccessToken

  val searchEndpoint = "https://api.spotify.com/v1/artists/"

  val albumFacade = AlbumFacade(identifier)
  val artistFacade = ArtistFacade(identifier)

  def analyse():Future[JsValue] = {
    for {
      albums <- albumFacade.getUsersAlbumCollection
      ids <- getIds(albums)
      artists <- getSeveralArtists(ids)
    } yield artists
  }

  def getIds(albums: Option[Map[String, Set[String]]]):Future[Set[Option[String]]] = {
    albums match {
      case Some(albs) =>
        val artists = albs.keySet
        Future.sequence {
          artists.map { artist =>
            artistFacade.getArtistByName(artist) flatMap {
              case Some(art) => Future.successful(art.spotifyId)
              case None => SpotifyApiFacade.getArtistId(artist)
            }
          }
        }
      case None => Future.successful(Set())
    }
  }

  def getSeveralArtists(ids: Set[Option[String]]):Future[JsValue] = {
    val searchList:Set[String] = ids.filter(_.isDefined).map(_.get)
    val results:Future[Set[JsValue]] = Future.sequence {
      searchList map { artistId =>
        val url = searchEndpoint + artistId + "/albums?market=DE&album_type=album"
        token match {
          case Some(t) =>
            WS.url(url).withHeaders("Authorization" -> s"Bearer $t").get() map { response =>
              Json.parse(response.body)
            }
          case None => Future.failed(new Exception("An access token could not be found"))
        }

      }
    }
    results map(_.head) // TODO
  }
}

object SpotifyAnalysis {
  def apply(identifier:Either[Int,String]) = new SpotifyAnalysis(identifier)
}
