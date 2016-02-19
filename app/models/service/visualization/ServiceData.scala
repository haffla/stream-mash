package models.service.visualization

import models.database.alias.Artist
import models.database.facade.ArtistFacade
import models.database.facade.service._
import models.util.{Constants, GroupMeasureConversion}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ServiceData(identifier:Either[Int,String]) extends GroupMeasureConversion {

  val services:List[String] = List(Constants.serviceSpotify, Constants.serviceDeezer, Constants.serviceNapster)
  val albumFacadesList:List[ServiceAlbumFacade] = List(SpotifyAlbumFacade, DeezerAlbumFacade, NapsterAlbumFacade)
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
      val serviceCountJson = services.foldLeft(Json.obj()) { (accumulated,curr) =>
        accumulated + (curr, countsForServiceFromList(albumCounts, Constants.serviceSpotify))
      }
      Json.obj(
        "user" -> artistsToJson(usersArtists),
        "total" -> toJson(totals),
        "missing" -> missingCounts
      ) ++ serviceCountJson
    }
  }

  private def countsForServiceFromList(counts: List[(String,Map[Long,Long])], serviceId: String): JsValue = {
    counts.find(_._1 == serviceId) match {
      case Some(elem) => toJson(elem._2)
      case _ => throw new Exception(s"Did not find stats for service $serviceId in result list")
    }
  }

  private def artistsToJson(artists:List[(Artist,Long,Double)]): List[JsValue] = {
    artists.map { case (artist,trackCount,score) =>
      Json.obj(
        "id" -> artist.id,
        "name" -> artist.name,
        "pic" -> artist.pic,
        "trackCount" -> trackCount,
        "score" -> score
      )
    }
  }

  private def missingAlbumCounts(artistIds: List[Long]): JsValue = {
    albumFacadesList.foldLeft(Json.obj()) { (accumulated, albumFacade) =>
      accumulated + (albumFacade.id, Json.toJson(albumFacade(identifier).countMissingUserAlbums(artistIds)))
    }
  }
}
