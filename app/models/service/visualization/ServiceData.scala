package models.service.visualization

import models.database.alias.Artist
import models.database.facade.ArtistFacade
import models.database.facade.service._
import models.util.{GroupMeasureConversion, Constants}
import org.squeryl.dsl.GroupWithMeasures
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ServiceData(identifier:Either[Int,String]) extends GroupMeasureConversion {

  def retrieve() = {
    val usersArtists = ArtistFacade(identifier).usersFavouriteArtistsWithTrackCountAndScore()
    val artistIds = usersArtists.map(_._1.id)
    val spoArtists = Future {
      val res = SpotifyArtistFacade.countArtistsAlbums(artistIds)
      toMap(res)
    }
    val deeArtists = Future {
      val res = DeezerArtistFacade.countArtistsAlbums(artistIds)
      toMap(res)
    }
    val napsArtists = Future {
      val res = NapsterArtistFacade.countArtistsAlbums(artistIds)
      toMap(res)
    }
    for {
      sp <- spoArtists
      dee <- deeArtists
      naps <- napsArtists
      missingCounts <- Future(missingAlbumCounts)
    } yield {
      val totals = mergeMaps(List(sp,dee,naps))
      Json.obj(
        "user" -> artistsToJson(usersArtists),
        Constants.serviceSpotify -> toJson(sp),
        Constants.serviceDeezer -> toJson(dee),
        Constants.serviceNapster -> toJson(naps),
        "total" -> toJson(totals),
        "missing" -> missingCounts
      )
    }
  }

  def artistsToJson(artists:List[(Artist,Long,Double)]):List[JsValue] = {
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

  def artistAlbumCountsToJson(counts:List[GroupWithMeasures[Long,Long]]):JsValue = {
    toJson(toMap(counts))
  }

  def missingAlbumCounts:JsValue = {
    val missingAlbumsOnSpotify = new SpotifyAlbumFacade(identifier).countMissingUserAlbums
    val missingAlbumsOnNapster = new NapsterAlbumFacade(identifier).countMissingUserAlbums
    val missingAlbumsOnDeezer = new DeezerAlbumFacade(identifier).countMissingUserAlbums
    Json.obj(
      Constants.serviceSpotify -> missingAlbumsOnSpotify,
      Constants.serviceDeezer -> missingAlbumsOnDeezer,
      Constants.serviceNapster -> missingAlbumsOnNapster
    )
  }
}
