package models.database.alias

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._

object AppDB extends Schema {
  val users = table[User]("account")
  val collections = table[UserCollection]("user_collection")
  val artists = table[Artist]("artist")
  val albums = table[Album]("album")
  val tracks = table[Track]("track")
  val userArtistLikings = table[UserArtistLiking]("user_artist_liking")
  val spotifyArtists = table[SpotifyArtist]("spotify_artist")
  val spotifyAlbums = table[SpotifyAlbum]("spotify_album")

  on(userArtistLikings)(ual => declare(
    columns(ual.userId, ual.artistId) are unique,
    columns(ual.userSession, ual.artistId) are unique,
    ual.id is autoIncremented("user_artist_liking_id_user_artist_liking_seq")
  ))

  on(collections)(c => declare(
    columns(c.userId, c.trackId) are unique,
    columns(c.userSession, c.trackId) are unique
  ))

  on(users)(u => declare(
    u.name is unique
  ))

  on(artists)(a => declare(
    a.name is unique,
    a.id is autoIncremented("artist_id_artist_seq")
  ))

  on(albums)(a => declare(
    columns(a.name, a.artistId) are unique,
    a.id is autoIncremented("album_id_album_seq")
  ))

  on(tracks)(t => declare(
    columns(t.name, t.artistId, t.albumId) are unique
  ))

  def getCollectionByUser(identifier:Either[Int,String]):List[(Album, Artist, Track, UserCollection)] = {
    transaction {
      val res = identifier match {
        case Left(fkUser) =>
          from(collections, tracks, albums, artists)((coll, tr, alb, art) =>
            where(coll.userId === fkUser and coll.trackId === tr.id and tr.albumId === alb.id and tr.artistId === art.id)
              select (alb,art,tr,coll)
          )
        case Right(session) =>
          from(collections, tracks, albums, artists)((coll, tr, alb, art) =>
            where(coll.userSession === Some(session) and coll.trackId === tr.id and tr.albumId === alb.id and tr.artistId === art.id)
              select (alb,art,tr,coll)
          )
      }
      res.toList
    }
  }
}