package models.database

import slick.driver.JdbcProfile
import slick.lifted.Index

trait MainDatabaseAccess {
  protected val driver: JdbcProfile
  import driver.api._

  class Account(tag: Tag) extends Table[alias.Account](tag, "account") {
    def id = column[Int]("id_user", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def password = column[String]("password")
    def itunesFileHash = column[String]("itunes_file_hash")
    def spotifyToken = column[String]("spotify_token")
    def rdioToken = column[String]("rdio_token")
    def deezerToken = column[String]("deezer_token")
    def soundcloudToken = column[String]("soundcloud_token")
    def lastfmToken = column[String]("lastfm_token")
    def index:Index = index("idx_name", name, unique=true)
    def * = (id.?, name, password, itunesFileHash.?, spotifyToken.?, rdioToken.?, deezerToken.?, soundcloudToken.?, lastfmToken.?) <> ((alias.Account.apply _).tupled, alias.Account.unapply _)
  }

  val accountQuery = TableQuery[Account]

  class Artist(tag:Tag) extends Table[alias.Artist](tag, "artist") {
    def id = column[Int]("id_artist", O.AutoInc, O.PrimaryKey)
    def name = column[String]("artist_name")
    def spotifyId = column[String]("spotify_id")
    def rdioId = column[String]("rdio_id")
    def soundcloudId = column[String]("soundcloud_id")
    def deezerId = column[String]("deezer_id")
    def lastfmId = column[String]("lastfm_id")
    def indexSpotify:Index = index("name_spotifyId", (name, spotifyId), unique=true)
    def indexRdio:Index = index("name_rdioId", (name, rdioId), unique=true)
    def indexSoundcloud:Index = index("name_soundcloudId", (name, soundcloudId), unique=true)
    def indexDeezer:Index = index("name_deezerId", (name, deezerId), unique=true)
    def indexLastfm:Index = index("name_lastfmId", (name, lastfmId), unique=true)
    def * = (id.?, name, spotifyId.?, rdioId.?, soundcloudId.?, deezerId.?, lastfmId.?) <> ((alias.Artist.apply _).tupled, alias.Artist.unapply _)
  }

  val artistQuery = TableQuery[Artist]

  class Album(tag:Tag) extends Table[alias.Album](tag, "album") {
    def id = column[Int]("id_album", O.AutoInc, O.PrimaryKey)
    def name = column[String]("album_name")
    def artistId = column[Int]("fk_artist")
    def indexWithId:Index = index("idx_name_id", (name,artistId), unique=true)
    def * = (id.?, name, artistId) <> ((alias.Album.apply _).tupled, alias.Album.unapply _)
  }

  val albumQuery = TableQuery[Album]

  class Track(tag:Tag) extends Table[alias.Track](tag, "track") {
    def id = column[Int]("track_id", O.AutoInc, O.PrimaryKey)
    def name = column[String]("track_name")
    def artistId = column[Int]("fk_artist")
    def albumId = column[Int]("album_id")
    def index:Index = index("track_idx", (name, artistId, albumId), unique=true)
    def * = (id.?, name, artistId, albumId.?) <> ((alias.Track.apply _).tupled, alias.Track.unapply _)
  }

  val trackQuery = TableQuery[Track]

}
