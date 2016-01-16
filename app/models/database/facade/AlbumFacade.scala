package models.database.facade

import models.database.alias.AppDB
import org.squeryl.PrimitiveTypeMode._

object AlbumFacade {
  def apply(identifier:Either[Int,String]) = new AlbumFacade(identifier)
}

class AlbumFacade(identifier:Either[Int,String]) extends Facade {
  def getUsersAlbums:List[Long] = {
    transaction {
      from(AppDB.albums, AppDB.collections, AppDB.tracks)((alb,coll,tr) =>
        where(tr.id === coll.trackId and tr.albumId === alb.id and AppDB.userWhereClause(coll,identifier))
          select alb.id
      ).distinct.toList
    }
  }
}
