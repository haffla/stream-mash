package models.database.alias

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._


object AppDB extends Schema {
  val users = table[User]("account")
  val collections = table[UserCollection]("user_collection")
  val artists = table[Artist]("artist")
  val albums = table[Album]("album")
  val tracks = table[Track]("track")

  on(collections)(c => declare(
    columns(c.userId, c.trackId) are unique,
    columns(c.userId, c.userSession) are unique
  ))

  on(users)(u => declare(
    u.name is unique
  ))

  on(artists)(a => declare(
    columns(a.name, a.spotifyId) are unique,
    columns(a.name, a.rdioId) are unique,
    columns(a.name, a.soundcloudId) are unique,
    columns(a.name, a.deezerId) are unique,
    columns(a.name, a.lastfmId) are unique
  ))

  on(albums)(a => declare(
    columns(a.name, a.artistId) are unique
  ))

  on(tracks)(t => declare(
    columns(t.name, t.artistId, t.albumId) are unique
  ))
}