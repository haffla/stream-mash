package models.database.facade.service

import models.database.alias.{Album, AppDB}
import org.squeryl.PrimitiveTypeMode._

trait ServiceAlbumFacade {

  def insertServiceAlbumIfNotExists(id:Long, serviceId:String):Long
  def insertAlbum(id:Long, serviceId:String):Long

  def saveAlbumWithNameAndId(albumName:String, artistId:Long, serviceId:String):Long = {
    transaction {
      from(AppDB.albums)(a =>
        where(a.name === albumName and a.artistId === artistId)
          select a.id
      ).headOption match {
        case Some(id) => insertServiceAlbumIfNotExists(id, serviceId)
        case _ =>
          val newAlbum:Album = AppDB.albums.insert(Album(albumName, artistId))
          insertAlbum(newAlbum.id, serviceId)
      }
    }
  }
}
