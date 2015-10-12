package models

import models.auth.MessageDigest
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.driver.JdbcProfile
import database.MainDatabaseAccess
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object User extends MainDatabaseAccess with HasDatabaseConfig[JdbcProfile] {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._

  def create(name:String, password:String):Future[Int] = {
    val hashedPassword = MessageDigest.digest(password)
    db.run(accountQuery returning accountQuery.map(_.id) += models.Account(name = name, password = hashedPassword))
  }

  def list:Future[Seq[User.Account#TableElementType]] = {
    db.run(accountQuery.result)
  }

  def exists(name: String):Future[Boolean] = {
    val account = accountQuery.filter(_.name === name)
    db.run(account.exists.result)
  }

  def saveItunesFileHash(userId:Int, hash:String) = {
    val iTunesFileHash = for { account <- accountQuery if account.id === userId } yield account.itunesFileHash
    db.run(iTunesFileHash.update(hash))
  }

  def iTunesFileProcessedAlready(userId:Int, hash:String):Future[Boolean] =  {
    db.run(accountQuery.filter(_.id === userId).result.map { account =>
      account.head.itunesFileHash match {
        case Some(s) =>
          if(s == hash) true else false
        case None => false
      }
    })
  }

}

case class UserData(name: String, password: String)
