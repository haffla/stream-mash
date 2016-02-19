package models.database.facade.service

import models.database.AppDB
import models.database.alias.Album
import org.squeryl.PrimitiveTypeMode._

trait ServiceAlbumFacade {

  val id:String

  def apply(identifier: Either[Int,String]): ServiceAlbumTrait

  def insertServiceAlbumIfNotExists(id:Long, serviceId:String):Long
  def insertAlbum(id:Long, serviceId:String):Long

  def saveAlbumWithNameAndId(albumName:String, artistId:Long, serviceId:String):Long = {
    inTransaction {
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
