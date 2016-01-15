package models.database.facade

import models.database.alias.AppDB
import org.squeryl.PrimitiveTypeMode._

object AlbumFacade {
  def apply(identifier:Either[Int,String]) = new AlbumFacade(identifier)
}

class AlbumFacade(identifier:Either[Int,String]) extends Facade {
  def getUsersAlbums:List[Long] = {
    transaction {
      val res  = identifier match {
        case Left(userId) =>
          from(AppDB.albums, AppDB.collections, AppDB.tracks)((alb,coll,tr) =>
            where(tr.id === coll.trackId and tr.albumId === alb.id and coll.userId === userId)
              select alb.id
          )
        case Right(session) =>
          from(AppDB.albums, AppDB.collections, AppDB.tracks)((alb,coll,tr) =>
            where(tr.id === coll.trackId and tr.albumId === alb.id and coll.userSession === Some(session))
              select alb.id
          )
      }
      res.distinct.toList
    }
  }
}
