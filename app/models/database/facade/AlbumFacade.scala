package models.database.facade

import models.database.alias.{Album, AppDB}
import org.squeryl.PrimitiveTypeMode._

object AlbumFacade {
  def saveByNameAndArtistId(album: String, artistId: Long) = {
    inTransaction {
      byNameAndArtistId(album, artistId) match {
        case Some(alb) => alb.id
        case _ => insert(album, artistId)
      }
    }
  }

  def insert(album:String, artistId:Long): Long = {
    AppDB.albums.insert(Album(album, artistId)).id
  }

  def byNameAndArtistId(name:String, artistId:Long) = {
    from(AppDB.albums)(a => where(a.name === name and a.artistId === artistId)
      select a
    ).headOption
  }

  def apply(identifier:Either[Int,String]) = new AlbumFacade(identifier)
}

class AlbumFacade(identifier:Either[Int,String]) extends Facade {

  def getUsersFavouriteAlbums(mostListenedToArtists:List[Long]):List[Album] = {
    transaction {
      join(
        AppDB.albums,
        AppDB.tracks,
        AppDB.collections,
        AppDB.userArtistLikings)((alb,tr,col,ual) =>
        where(ual.score.gt(0) and alb.artistId.in(mostListenedToArtists) and AppDB.userWhereClause(ual,identifier) and AppDB.userWhereClause(col,identifier))
          select alb
          on(
          alb.id === tr.albumId,
          col.trackId === tr.id,
          ual.artistId === alb.artistId
          )
      ).distinct.toList
    }
  }
}
