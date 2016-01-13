package models.database.facade

import models.database.alias.{SpotifyArtist, Artist, AppDB}
import org.squeryl.PrimitiveTypeMode._

object SpotifyArtistFacade {

  def saveArtistWithName(artistName:String):Long = {
    transaction {
      from(AppDB.artists)(a =>
        where(a.name === artistName)
          select a.id
      ).headOption match {
        case Some(artistId) => insertArtist(artistId)
        case _ =>
          val newArtist:Artist = AppDB.artists.insert(Artist(artistName))
          AppDB.spotifyArtist.insert(SpotifyArtist(newArtist.id)).id

      }
    }
  }

  def insertArtist(id:Long):Long = {
    from(AppDB.spotifyArtist)(sa =>
      where(sa.id === id)
        select sa.id
    ).headOption match {
      case None => AppDB.spotifyArtist.insert(SpotifyArtist(id)).id
      case _ => id
    }
  }
}
