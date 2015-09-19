package models

import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.driver.JdbcProfile
import tables.AccountTable
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object User extends AccountTable with HasDatabaseConfig[JdbcProfile] {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._

  val accountQuery = TableQuery[Account]

  def create(name:String, password:String): Boolean = {
    val exists = db.run(accountQuery.filter(_.name === name).exists.result)
    val success = exists match {
      case f:Future[Boolean] =>
        f map(
          bool => if(!bool) db.run(accountQuery += models.Account(0, name, password))
          /*
          0 or any integer here. the value will be ignored by slick/database as it
          is defined as auto increment id.
          */
          )
        true // if there is no existing user in db and new user was saved
      case _ => false
    }
    success
  }

  def list:Future[Seq[User.Account#TableElementType]] = {
    db.run(accountQuery.result)
  }

}

case class UserData(name: String, password: String)
