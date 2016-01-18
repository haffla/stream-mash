package models.database.facade.service

import models.database.alias.AppDB
import models.database.alias.service.DeezerAlbum
import org.squeryl.PrimitiveTypeMode._

object DeezerAlbumFacade extends ServiceAlbumFacade {

  override def insertServiceAlbumIfNotExists(id:Long, serviceId:String):Long = {
    from(AppDB.deezerAlbums)(da =>
      where(da.id === id)
        select da.id
    ).headOption match {
      case None => insertAlbum(id, serviceId)
      case _ => id
    }
  }

  override def insertAlbum(id: Long, serviceId: String): Long = {
    AppDB.deezerAlbums.insert(DeezerAlbum(id, serviceId)).id
  }
}
