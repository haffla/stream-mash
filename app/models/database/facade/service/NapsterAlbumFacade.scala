package models.database.facade.service

import models.database.alias.AppDB
import models.database.alias.service.NapsterAlbum
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
}
