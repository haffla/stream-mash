package models.database.facade

import models.database.AppDB
import models.database.alias.Track
import org.squeryl.PrimitiveTypeMode._

object TrackFacade {
  def saveTrack(track: String, artistId: Long, albumId: Long):Long = {
    inTransaction {
      byNameAndArtistIdAndAlbumId(track, artistId, albumId) match {
        case Some(tr) => tr.id
        case _ => insert(track, artistId, albumId)
      }
    }
  }

  def insert(trackName: String, artistId: Long, albumId: Long):Long = {
    val track = Track(name = trackName, artistId = artistId, albumId = Some(albumId))
    AppDB.tracks.insert(track).id
  }

  def byNameAndArtistIdAndAlbumId(track:String, artistId:Long, albumId:Long):Option[Track] = {
    from(AppDB.tracks)(t =>
      where(lower(t.name) === track.toLowerCase and t.artistId === artistId and t.albumId === albumId)
        select t
    ).headOption
  }

  def apply(identifier:Either[Int,String]) = new TrackFacade(identifier)
}

class TrackFacade(identifier:Either[Int,String]) extends Facade {
  def getUsersTracksForAlbum(albumName:String):List[String] = {
    transaction {
      join(AppDB.tracks, AppDB.albums, AppDB.collections)((tr,alb,coll) =>
        where(alb.name === albumName and AppDB.userWhereClause(coll, identifier))
          select tr.name
          on(
          tr.albumId === alb.id,
          tr.id === coll.trackId
          )
      ).toList
    }
  }
}
