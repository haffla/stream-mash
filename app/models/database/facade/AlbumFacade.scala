package models.database.facade

import models.database.MainDatabaseAccess
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import scalikejdbc._
import slick.driver.JdbcProfile

import scala.concurrent.Future

object AlbumFacade extends MainDatabaseAccess with HasDatabaseConfig[JdbcProfile] {

  import driver.api._
  implicit val session = AutoSession
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  def deleteUsersAlbums(identifier:Either[Int,String]):Future[Int] = {
    val query = identifier match {
      case Left(userId) =>
        albumQuery.filter(_.idUser === userId)
      case Right(sessionKey) =>
        albumQuery.filter(_.userSessionKey === sessionKey)
    }
    db.run(query.delete)
  }
}
