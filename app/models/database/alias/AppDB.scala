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

  val artistToAlbums = oneToManyRelation(artists, albums).via((art, alb) => art.id === alb.artistId)

  /**
   * Currently only returns artists and albums connected to a user
   */
  def getCollectionByUser(identifier:Either[Int,String]):List[(Album, Artist)] = {
    transaction {
      val res = identifier match {
        case Left(fkUser) =>
          from(collections, tracks, albums, artists)((coll, tr, alb, art) =>
            where(coll.userId === fkUser and coll.trackId === tr.id and tr.albumId === alb.id and tr.artistId === art.id)
              select (alb,art)
          )
        case Right(session) =>
          from(collections, tracks, albums, artists)((coll, tr, alb, art) =>
            where(coll.userSession === Some(session) and coll.trackId === tr.id and tr.albumId === alb.id and tr.artistId === art.id)
              select (alb,art)
          )
      }
      res.toList
    }
  }
}