package tables

import slick.driver.JdbcProfile
import slick.lifted.{ForeignKeyQuery, Index}

trait UserCollectionTable {
  protected val driver: JdbcProfile
  import driver.api._

  class Account(tag: Tag) extends Table[models.Account](tag, "account") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def password = column[String]("password")
    def index:Index = index("idx_name", name, unique=true)
    def * = (id, name, password) <> ((models.Account.apply _).tupled, models.Account.unapply _)
  }

  class Album(tag:Tag) extends Table[models.music.Album](tag, "album") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def interpret = column[String]("interpret")
    def index:Index = index("name_interpret", (name,interpret), unique=true)
    def * = (id, name, interpret) <> ((models.music.Album.apply _).tupled, models.music.Album.unapply _)
  }

  class Interpret(tag: Tag) extends Table[models.music.Interpret](tag, "interpret") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def index:Index = index("interpret_name", name, unique = true)
    def * = (id,name) <> ((models.music.Interpret.apply _).tupled, models.music.Interpret.unapply _)
  }


  val artistQuery = TableQuery[Interpret]
  val albumQuery = TableQuery[Album]
  val userQuery = TableQuery[Account]

  class UserCollection(tag:Tag) extends Table[models.music.UserCollection](tag, "user_collection") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def id_interpret = column[Int]("id")
    def id_album = column[Int]("id")
    def id_user = column[Int]("id")
    def interpret = foreignKey("fk_interpret", id_interpret, artistQuery)(_.id)
    def album = foreignKey("fk_album", id_album, albumQuery)(_.id)
    def user = foreignKey("fk_user", id_user, userQuery)(_.id)
    def * = (id, id_interpret, id_album) <> ((models.music.UserCollection.apply _).tupled, models.music.UserCollection.unapply _)
  }
}
