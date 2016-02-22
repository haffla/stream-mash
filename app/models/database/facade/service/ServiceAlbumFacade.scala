package models.database.facade.service

import models.database.facade.AlbumFacade
import org.squeryl.PrimitiveTypeMode._

trait ServiceAlbumFacade {

  val serviceId:String

  def apply(identifier: Either[Int,String]): ServiceAlbumTrait

  def insertServiceAlbumIfNotExists(id:Long, serviceId:String):Long
  def insertAlbum(id:Long, serviceId:String):Long

  def saveAlbumWithNameAndId(albumName:String, artistId:Long, serviceId:String):Long = {
    inTransaction {
      val albumId = AlbumFacade.saveByNameAndArtistId(albumName, artistId)
      insertServiceAlbumIfNotExists(albumId, serviceId)
    }
  }
}
