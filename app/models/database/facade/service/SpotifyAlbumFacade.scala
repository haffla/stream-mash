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
