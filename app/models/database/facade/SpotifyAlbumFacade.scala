package models.database.facade

import models.database.alias.{AppDB, Album, SpotifyAlbum}
import org.squeryl.PrimitiveTypeMode._

object SpotifyAlbumFacade {

  def saveAlbumWithNameAndId(albumName:String, artistId:Long, spotifyId:String):Long = {
    transaction {
      from(AppDB.albums)(a =>
        where(a.name === albumName and a.artistId === artistId)
          select a.id
      ).headOption match {
        case Some(id) => insertAlbum(id, spotifyId)
        case _ =>
          val newAlbum:Album = AppDB.albums.insert(Album(albumName, artistId))
          AppDB.spotifyAlbum.insert(SpotifyAlbum(newAlbum.id, spotifyId)).id
      }
    }
  }

  def insertAlbum(id:Long, spotifyId:String):Long = {
    from(AppDB.spotifyAlbum)(sa =>
      where(sa.id === id)
        select sa.id
    ).headOption match {
      case None => AppDB.spotifyAlbum.insert(SpotifyAlbum(id, spotifyId)).id
      case _ => id
    }
  }
}
