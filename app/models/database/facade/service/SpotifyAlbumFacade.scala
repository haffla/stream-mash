package models.database.facade.service

import models.database.alias.AppDB
import models.database.alias.service.SpotifyAlbum
import org.squeryl.PrimitiveTypeMode._

object SpotifyAlbumFacade extends ServiceAlbumFacade {

  override def insertServiceAlbumIfNotExists(id:Long, serviceId:String):Long = {
    from(AppDB.spotifyAlbums)(sa =>
      where(sa.id === id)
        select sa.id
    ).headOption match {
      case None => insertAlbum(id, serviceId)
      case _ => id
    }
  }

  override def insertAlbum(id: Long, serviceId: String): Long = {
    AppDB.spotifyAlbums.insert(SpotifyAlbum(id, serviceId)).id
  }
}

class SpotifyAlbumFacade(identifier:Either[Int,String]) {

  def countMissingUserAlbums:Long = {
    inTransaction {
      join(AppDB.albums, AppDB.tracks, AppDB.collections, AppDB.spotifyAlbums.leftOuter)((alb,tr,col,spAlb) =>
        where(AppDB.userWhereClause(col, identifier) and spAlb.map(_.id).isNull)
          compute count(alb.id)
          on(
          tr.albumId === alb.id,
          col.trackId === tr.id,
          spAlb.map(_.id) === alb.id
          )
      ).toLong
    }
  }
}
