package tables

import slick.driver.JdbcProfile
import slick.lifted.Index

trait AccountTable {

  protected val driver: JdbcProfile
  import driver.api._
  class Account(tag: Tag) extends Table[models.Account](tag, "account") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def password = column[String]("password")
    //def index:Index = index("idx_name", name, unique=true)
    def * = (id, name, password) <> ((models.Account.apply _).tupled, models.Account.unapply _)
  }
}