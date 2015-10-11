package tables

import models.Account
import slick.driver.JdbcProfile
import slick.lifted.Index

trait AccountTable {
  protected val driver: JdbcProfile
  import driver.api._

  class Account(tag: Tag) extends Table[models.Account](tag, "account") {
    def id = column[Int]("id_user", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def password = column[String]("password")
    def itunesFileHash = column[String]("itunes_file_hash")
    def index:Index = index("idx_name", name, unique=true)
    def * = (id.?, name, password, itunesFileHash.?) <> ((models.Account.apply _).tupled, models.Account.unapply _)
  }

  val accountQuery = TableQuery[Account]

  class Album(tag:Tag) extends Table[models.music.Album](tag, "album") {
    def id = column[Int]("id_album", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def interpret = column[String]("interpret")
    def id_user = column[Int]("fk_user")
    def fk_user = foreignKey("id_user", id_user, accountQuery)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def index:Index = index("name_interpret", (name,interpret,id_user), unique=true)
    def * = (id.?, name, interpret, id_user) <> ((models.music.Album.apply _).tupled, models.music.Album.unapply _)
  }

  val albumQuery = TableQuery[Album]

}
