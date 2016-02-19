package models.database.facade.service

import models.database.AppDB
import models.database.alias.service.NapsterAlbum
import models.util.Constants
import org.squeryl.PrimitiveTypeMode._

object NapsterAlbumFacade extends ServiceAlbumFacade {

  override def insertServiceAlbumIfNotExists(id:Long, serviceId:String):Long = {
    from(AppDB.napsterAlbums)(na =>
      where(na.id === id)
        select na.id
    ).headOption match {
      case None => insertAlbum(id, serviceId)
      case _ => id
    }
  }

  override def insertAlbum(id: Long, serviceId: String): Long = {
    AppDB.napsterAlbums.insert(NapsterAlbum(id, serviceId)).id
  }

  override def apply(identifier: Either[Int, String]): ServiceAlbumTrait = new NapsterAlbumFacade(identifier)

  override val id: String = Constants.serviceNapster
}

class NapsterAlbumFacade(identifier:Either[Int,String]) extends ServiceAlbumTrait {

  def countMissingUserAlbums(artistIds:List[Long]):Long = {
    inTransaction {
      join(AppDB.albums, AppDB.tracks, AppDB.collections, AppDB.napsterAlbums.leftOuter)((alb,tr,col,napsAlb) =>
        where(alb.id.in(artistIds:List[Long]) and AppDB.userWhereClause(col, identifier) and napsAlb.map(_.id).isNull)
          compute countDistinct(alb.id)
          on(
          tr.albumId === alb.id,
          col.trackId === tr.id,
          napsAlb.map(_.id) === alb.id
          )
      ).toLong
    }
  }
}
