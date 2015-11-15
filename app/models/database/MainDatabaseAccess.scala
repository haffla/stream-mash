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
    def id_user = column[Int]("fk_user")
    def fk_user = foreignKey("id_user", id_user, accountQuery)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def index:Index = index("name_interpret", (name,interpret,id_user), unique=true)
    def * = (id.?, name, interpret, id_user) <> ((alias.Album.apply _).tupled, alias.Album.unapply _)
  }

  val albumQuery = TableQuery[Album]

  class Artist(tag:Tag) extends Table[alias.Artist](tag, "artist") {
    def id = column[Int]("id_artist", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def spotifyId = column[String]("spotify_id")
    def rdioId = column[String]("rdio_id")
    def indexSpotify:Index = index("name_spotifyId", (name, spotifyId), unique=true)
    def indexRdio:Index = index("name_rdioId", (name, rdioId), unique=true)
    def * = (id.?, name, spotifyId.?, rdioId.?) <> ((alias.Artist.apply _).tupled, alias.Artist.unapply _)
  }

  val artistQuery = TableQuery[Artist]

}
