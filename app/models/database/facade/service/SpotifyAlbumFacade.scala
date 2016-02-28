package models.database.facade.service

import models.database.AppDB
import models.database.alias.service.SpotifyAlbum
import models.util.Constants
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

  override def apply(identifier: Either[Int, String]): ServiceAlbumTrait = new SpotifyAlbumFacade(identifier)

  override val serviceId: String = Constants.serviceSpotify
}

class SpotifyAlbumFacade(identifier:Either[Int,String]) extends ServiceAlbumTrait {

  def countMissingUserAlbums(artistIds:List[Long]): Long = {
    inTransaction {
      join(AppDB.albums, AppDB.tracks, AppDB.collections, AppDB.spotifyAlbums.leftOuter)((alb,tr,col,spAlb) =>
        where(alb.artistId.in(artistIds) and alb.name <> Constants.unknownAlbum and AppDB.userWhereClause(col, identifier) and spAlb.map(_.id).isNull)
          compute countDistinct(alb.id)
          on(
          tr.albumId === alb.id,
          col.trackId === tr.id,
          spAlb.map(_.id) === alb.id
          )
      ).toLong
    }
  }
}
