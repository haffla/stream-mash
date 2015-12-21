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

import scalikejdbc._

import scala.concurrent.Future

class User(identifier:Either[Int, String]) extends MainDatabaseAccess with HasDatabaseConfig[JdbcProfile] {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  implicit val session = AutoSession

  def getServiceToken(service:String):Option[String] = {
    identifier match {
      case Left(id) =>
        val tokenField = service match {
          case "spotify" => sqls"spotify_token"
          case _ => throw new IllegalArgumentException("The given service '$service' is not supported")
        }
        sql"select $tokenField from account where id_user=$id".map(rs => rs.string(service + "_token")).single.apply()
      case Right(_) => None
    }
  }

  def setServiceToken(service:String, token:String) = {
    identifier match {
      case Left(id) =>
        val field = service match {
          case "spotify" => sqls"spotify_token"
          case "rdio" => sqls"rdio_token"
          case "deezer" => sqls"deezer_token"
          case "soundcloud" => sqls"soundcloud_token"
          case "lastfm" => sqls"lastfm_token"
          case _ => throw new IllegalArgumentException("The given service '$service' is not supported")
        }
        sql"update account set $field=$token where id_user=$id".update.apply()

      case Right(_) =>
    }
  }

  import driver.api._

  def saveItunesFileHash(hash:String) = {
    identifier match {
      case Left(userId) =>
        val iTunesFileHash = for { account <- accountQuery if account.id === userId } yield account.itunesFileHash
        db.run(iTunesFileHash.update(hash))
      case Right(sessionKey) =>
        Cache.set(Constants.fileHashCacheKeyPrefix + sessionKey, hash)
    }
  }

  def iTunesFileProcessedAlready(hash:String):Future[Boolean] =  {
    identifier match {
      case Left(userId) =>
        db.run(accountQuery.filter(_.id === userId).result map { account =>
          account.head.itunesFileHash match {
            case Some(s) => s == hash
            case None => false
          }
        })
      case Right(sessionKey) =>
        val result = Cache.getAs[String](Constants.fileHashCacheKeyPrefix + sessionKey) match {
          case Some(storedHash) => storedHash == hash
          case None => false
        }
        Future.successful(result)
    }
  }
}

object User extends MainDatabaseAccess with HasDatabaseConfig[JdbcProfile] {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._

  def apply(identifier:Either[Int,String]) = new User(identifier)

  /**
   * Updates all album entities of the session identified user
   * with his userId in order to prevent loss of data for the user
  //TODO
  def transferData(userId: Int, sessionKey: String) = {
    db.run(
      albumQuery.filter(_.userSessionKey === sessionKey)
        .map(a => (a.idUser, a.userSessionKey))
        .update(userId, null)
    )
  }
   */

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

  def getAccountByUserName(username:String) = {
    val account = accountQuery.filter(_.name === username).take(1)
    db.run(account.result.headOption)
  }
  //TODO
  /*def deleteUsersAlbumCollection(id:Int):Future[Int] = {
    db.run(albumQuery.filter(a => a.idUser === id).delete)
  }*/

}

case class UserData(name: String, password: String)
