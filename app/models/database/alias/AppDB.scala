package models.database.alias

import models.database.alias.service._
import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.ast.LogicalBoolean

object AppDB extends Schema {
  val users = table[User]("account")
  val collections = table[UserCollection]("user_collection")
  val artists = table[Artist]("artist")
  val albums = table[Album]("album")
  val tracks = table[Track]("track")
  val userArtistLikings = table[UserArtistLiking]("user_artist_liking")
  val spotifyArtists = table[SpotifyArtist]("spotify_artist")
  val spotifyAlbums = table[SpotifyAlbum]("spotify_album")
  val deezerArtists = table[DeezerArtist]("deezer_artist")
  val deezerAlbums = table[DeezerAlbum]("deezer_album")
  val napsterArtists = table[NapsterArtist]("napster_artist")
  val napsterAlbums = table[NapsterAlbum]("napster_album")
  val serviceArtistAbsence = table[ServiceArtistAbsence]("service_artist_absence")

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

  on(serviceArtistAbsence)(saa => declare(
    columns(saa.artistId, saa.userId, saa.service) are unique,
    columns(saa.artistId, saa.userSession, saa.service) are unique,
    saa.id is autoIncremented("service_artist_absence_id_service_artist_absence_seq")
  ))

  def getCollectionByUser(identifier:Either[Int,String]):List[(Album, Artist, Track, UserCollection)] = {
    transaction {
      from(collections, tracks, albums, artists)((coll, tr, alb, art) =>
        where(userWhereClause(coll, identifier) and coll.trackId === tr.id and tr.albumId === alb.id and tr.artistId === art.id)
          select (alb,art,tr,coll)
      ).toList
    }
  }

  def userWhereClause(userRelatedEntity:HasUserOrSession, id:Either[Int,String]):LogicalBoolean = {
    id match {
      case Left(i) => userRelatedEntity.getUserId === i
      case Right(userSession) => userRelatedEntity.getUserSession === Some(userSession)
    }
  }
}