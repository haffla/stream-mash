package models.service.visualization

import models.database.alias.Artist
import models.database.facade.ArtistFacade
import models.database.facade.service._
import models.util.GroupMeasureConversion
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ServiceData(identifier:Either[Int,String]) extends GroupMeasureConversion {

  val serviceAlbumFacadeList:List[ServiceAlbumFacade] = List(SpotifyAlbumFacade, DeezerAlbumFacade, NapsterAlbumFacade)
  val serviceArtistFacadeList: List[ServiceArtistTrait] = List(SpotifyArtistFacade, DeezerArtistFacade, NapsterArtistFacade)

  def retrieve() = {
    val usersArtists = ArtistFacade(identifier).usersFavouriteArtistsWithTrackCountAndScore()
    val artistIds = usersArtists.map(_._1.id)
    val result:Future[List[(String, Map[Long, Long])]] = Future.sequence {
      serviceArtistFacadeList.map { serviceArtistFacade =>
        Future {
          val (serviceId, counts) = serviceArtistFacade.countArtistsAlbums(artistIds)
          (serviceId, toMap(counts))
        }
      }
    }
    for {
      albumCounts <- result
      missingCounts <- Future(missingAlbumCounts(artistIds))
    } yield {
      val totals = mergeMaps(albumCounts.map(_._2))
      val serviceCountJson = albumCounts.foldLeft(Json.obj()) { (accumulated,albumCount) =>
        accumulated + (albumCount._1, toJson(albumCount._2))
      }
      Json.obj(
        "user" -> artistsToJson(usersArtists),
        "total" -> toJson(totals),
        "missing" -> missingCounts
      ) ++ serviceCountJson
    }
  }

  private def artistsToJson(artists:List[(Artist,Long,Double)]): List[JsValue] = {
    artists.map { case (artist,trackCount,score) =>
      Json.obj(
        "id" -> artist.id,
        "name" -> artist.name,
        "trackCount" -> trackCount,
        "score" -> score
      )
    }
  }

  private def missingAlbumCounts(artistIds: List[Long]): JsValue = {
    serviceAlbumFacadeList.foldLeft(Json.obj()) { (accumulated, albumFacade) =>
      accumulated + (albumFacade.serviceId, Json.toJson(albumFacade(identifier).countMissingUserAlbums(artistIds)))
    }
  }
}
