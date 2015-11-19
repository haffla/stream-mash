package models

import models.auth.MessageDigest
import models.database.MainDatabaseAccess
import models.database.alias.Account
import models.service.Constants
import play.api.Play
import play.api.cache.Cache
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play.current
import slick.driver.JdbcProfile

import scala.concurrent.Future

object User extends MainDatabaseAccess with HasDatabaseConfig[JdbcProfile] {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._

  /**
   * Updates all album entities of the session identified user
   * with his userId in order to prevent loss of data for the user
   */
  def transferData(userId: Int, sessionKey: String) = {
    db.run(
      albumQuery.filter(_.userSessionKey === sessionKey)
        .map(a => (a.idUser, a.userSessionKey))
        .update(userId, null)
    )
  }

  def create(name:String, password:String):Future[Int] = {
    val hashedPassword = MessageDigest.digest(password)
    db.run(accountQuery returning accountQuery.map(_.id) += Account(name = name, password = hashedPassword))
  }

  def list:Future[Seq[User.Account#TableElementType]] = {
    db.run(accountQuery.result)
  }

  def exists(name: String):Future[Boolean] = {
    val account = accountQuery.filter(_.name === name)
    db.run(account.exists.result)
  }

  def saveItunesFileHash(identifier: Either[Int, String], hash:String) = {
    identifier match {
      case Left(userId) =>
        val iTunesFileHash = for { account <- accountQuery if account.id === userId } yield account.itunesFileHash
        db.run(iTunesFileHash.update(hash))
      case Right(sessionKey) =>
        Cache.set(Constants.fileHashCacheKeyPrefix + sessionKey, hash)
    }
  }

  def iTunesFileProcessedAlready(identifier: Either[Int, String], hash:String):Future[Boolean] =  {
    identifier match {
      case Left(userId) =>
        db.run(accountQuery.filter(_.id === userId).result map { account =>
          account.head.itunesFileHash match {
            case Some(s) =>
              s == hash
            case None => false
          }
        })
      case Right(sessionKey) =>
        val result = Cache.getAs[String](Constants.fileHashCacheKeyPrefix + sessionKey) match {
          case Some(storedHash) =>
            storedHash == hash
          case None => false
        }
        Future.successful(result)
    }
  }

  def getAccountByUserName(user:UserData) = {
    val account = accountQuery.filter(_.name === user.name).take(1)
    db.run(account.result.headOption)
  }

}

case class UserData(name: String, password: String)
