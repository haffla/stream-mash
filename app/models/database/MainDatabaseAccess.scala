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
    def index:Index = index("idx_name", name, unique=true)
    def * = (id.?, name, password, itunesFileHash.?) <> ((alias.Account.apply _).tupled, alias.Account.unapply _)
  }

  val accountQuery = TableQuery[Account]

  class Album(tag:Tag) extends Table[alias.Album](tag, "album") {
    def id = column[Int]("id_album", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def interpret = column[String]("interpret")
    def idUser = column[Int]("fk_user")
    def userSessionKey = column[String]("user_session_key")
    def fkUser = foreignKey("id_user", idUser, accountQuery)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def indexWithId:Index = index("name_interpret_id", (name,interpret,idUser), unique=true)
    def indexWithSession:Index = index("name_interpret_session", (name,interpret, userSessionKey), unique=true)
    def * = (id.?, name, interpret, idUser.?, userSessionKey.?) <> ((alias.Album.apply _).tupled, alias.Album.unapply _)
  }

  val albumQuery = TableQuery[Album]

  class Artist(tag:Tag) extends Table[alias.Artist](tag, "artist") {
    def id = column[Int]("id_artist", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def spotifyId = column[String]("spotify_id")
    def rdioId = column[String]("rdio_id")
    def soundcloudId = column[String]("soundcloud_id")
    def deezerId = column[String]("deezer_id")
    def indexSpotify:Index = index("name_spotifyId", (name, spotifyId), unique=true)
    def indexRdio:Index = index("name_rdioId", (name, rdioId), unique=true)
    def indexSoundcloud:Index = index("name_soundcloudId", (name, soundcloudId), unique=true)
    def indexDeezer:Index = index("name_deezerId", (name, deezerId), unique=true)
    def * = (id.?, name, spotifyId.?, rdioId.?, soundcloudId.?, deezerId.?) <> ((alias.Artist.apply _).tupled, alias.Artist.unapply _)
  }

  val artistQuery = TableQuery[Artist]

}
