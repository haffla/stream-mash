package models

import models.auth.MessageDigest
import models.database.alias.AppDB
import models.database.facade.Services
import models.service.Constants
import play.api.cache.Cache
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play.current
import org.squeryl.PrimitiveTypeMode._

import scalikejdbc._

import scala.concurrent.Future

class User(identifier:Either[Int, String]) {

  implicit val session = User.session

  def getServiceToken(service:String):Option[String] = {
    identifier match {
      case Left(id) =>
        val tokenField = Services.getFieldForService(service)
        sql"select $tokenField from account where id_user=$id".map(rs => rs.string(tokenField.value)).single().apply()
      case Right(_) => None
    }
  }

  def getServiceRefreshToken(service:String):Option[String] = {
    identifier match {
      case Left(id) =>
        val tokenField = Services.getRefreshFieldForService(service)
        sql"select $tokenField from account where id_user=$id".map(rs => rs.string(service + "_token_refresh")).single().apply()
      case Right(_) => None
    }
  }

  def setServiceToken(service:String, token:String, refreshToken:Option[String]) = {
    identifier match {
      case Left(id) =>
        val field = Services.getFieldForService(service)
        val sql = refreshToken match {
          case Some(refreshTkn) =>
            val refreshTokenField = Services.getRefreshFieldForService(service)
            sql"update account set $field=$token, $refreshTokenField=$refreshTkn where id_user=$id"
          case None =>
            sql"update account set $field=$token where id_user=$id"
        }
        sql.update().apply()
      case Right(_) =>
    }
  }

  def deleteUsersCollection() = {
    lazy val action = identifier match {
      case Left(id) => AppDB.collections.deleteWhere(c => c.userId === id)
      case Right(sessionKey) => AppDB.collections.deleteWhere(c => c.userSession like sessionKey)
    }
    transaction(action)
  }

  def saveItunesFileHash(hash:String) = {
    identifier match {
      case Left(userId) =>
        sql"update account set itunes_file_hash = $hash where id_user = $userId".update().apply()
      case Right(sessionKey) =>
        Cache.set(Constants.fileHashCacheKeyPrefix + sessionKey, hash)
    }
  }

  def iTunesFileProcessedAlready(hash:String):Future[Boolean] = {
    Future {
      identifier match {
        case Left(userId) =>
          sql"select itunes_files_hash from account where fk_user = $userId".map(rs => rs.string("itunes_file_hash")).single().apply() match {
            case Some(storedHash) => storedHash == hash
            case None => false
          }
        case Right(sessionKey) =>
          val result:Boolean = Cache.getAs[String](Constants.fileHashCacheKeyPrefix + sessionKey) match {
            case Some(storedHash) => storedHash == hash
            case None => false
          }
          result
      }
    }
  }
}

object User {

  implicit val session = AutoSession

  def getAnyAccessToken(service: String): Option[String] = {
    val tokenField = Services.getFieldForService(service)
    sql"select $tokenField from account where $tokenField is not null"
      .map(rs => rs.string(tokenField.value)).single().apply()
  }

  def getAnyAccessTokenPair(service:String):Option[models.Tokens] = {
    val tokenField = Services.getFieldForService(service)
    val refreshTokenField = Services.getRefreshFieldForService(service)
    sql"select $tokenField, $refreshTokenField from account where $tokenField is not null"
      .map(rs => models.Tokens(rs.string(tokenField.value), rs.string(refreshTokenField.value)))
        .single().apply()
  }

  def apply(identifier:Either[Int,String]) = new User(identifier)

  def getAccountByUserName(username:String):Future[Option[database.alias.User]] = {
    Future {
      transaction {
        AppDB.users.where(u => u.name === username).headOption
      }
    }
  }

  /**
   * Updates all album entities of the session identified user
   * with his userId in order to prevent loss of data for the user
   */
  def transferData(userId: Long, sessionKey: String) = {
    Array(sqls"user_collection", sqls"user_artist_liking").foreach { table =>
      sql"update $table set fk_user=$userId,user_session=null where user_session = $sessionKey".update().apply()
    }

    //sql"update user_artist_liking set fk_user=$userId,user_session=null where user_session = $sessionKey".update().apply()
  }
  def exists(name: String):Future[Boolean] = {
    Future {
      sql"select id_user from account where name = $name".map(rs => rs.int("id_user")).single().apply() match {
        case Some(_) => true
        case None => false
      }
    }
  }

  def create(name:String, password:String):Future[Long] = {
    Future {
      val hashedPassword = MessageDigest.digest(password)
      sql"insert into account (name, password) values ($name, $hashedPassword)".updateAndReturnGeneratedKey().apply()
    }
  }

  def list:Future[Seq[database.alias.User]] = {
    Future {
      transaction(AppDB.users.toList)
    }
  }

}

case class UserData(name: String, password: String)
case class Tokens(accessToken:String, refreshToken:String)
