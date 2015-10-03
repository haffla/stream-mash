package models

import models.auth.RosettaSHA256
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.driver.JdbcProfile
import tables.AccountTable
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object User extends AccountTable with HasDatabaseConfig[JdbcProfile] {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._

  def create(name:String, password:String):Future[Int] = {
    val hashedPassword = RosettaSHA256.digest(password)
    db.run(accountQuery returning accountQuery.map(_.id) += models.Account(0, name, hashedPassword))
  }

  def list:Future[Seq[User.Account#TableElementType]] = {
    db.run(accountQuery.result)
  }

  def exists(name: String):Future[Boolean] = {
    val account = accountQuery.filter(_.name === name)
    db.run(account.exists.result)
  }

}

case class UserData(name: String, password: String)
