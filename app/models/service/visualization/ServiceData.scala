package models.service.visualization

import models.database.alias.Artist
import models.database.facade.ArtistFacade
import models.database.facade.service.{DeezerArtistFacade, NapsterArtistFacade, SpotifyArtistFacade}
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
      artistAlbumCountsToJson(res)
    }
    val deeArtists = Future {
      val res = DeezerArtistFacade.countArtistsAlbums(artistIds)
      artistAlbumCountsToJson(res)
    }
    val napsArtists = Future {
      val res = NapsterArtistFacade.countArtistsAlbums(artistIds)
      artistAlbumCountsToJson(res)
    }
    for {
      sp <- spoArtists
      dee <- deeArtists
      naps <- napsArtists
    } yield {
      Json.obj(
        "user" -> artistsToJson(usersArtists),
        Constants.serviceSpotify -> sp,
        Constants.serviceDeezer -> dee,
        Constants.serviceNapster -> naps
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
}
