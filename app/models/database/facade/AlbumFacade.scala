package models.database.facade

import models.database.MainDatabaseAccess
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import scalikejdbc._
import slick.driver.JdbcProfile

import scala.concurrent.Future

object AlbumFacade {
  def apply(identifier:Either[Int,String]) = new AlbumFacade(identifier)
}

class AlbumFacade(identifier:Either[Int,String]) extends MainDatabaseAccess with HasDatabaseConfig[JdbcProfile] {

  import driver.api._
  implicit val session = AutoSession
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  def deleteUsersAlbums():Future[Int] = {
    val query = identifier match {
      case Left(userId) =>
        albumQuery.filter(_.idUser === userId)
      case Right(sessionKey) =>
        albumQuery.filter(_.userSessionKey === sessionKey)
    }
    db.run(query.delete)
  }

  def findSingleUserAlbum(name: String, interpret:String):Future[Seq[AlbumFacade.this.Album#TableElementType]] = {
    db.run(albumQuery.filter { albums =>
      identifier match {
        case Left(userId) =>
          albums.name === name && albums.interpret === interpret && albums.idUser === userId
        case Right(sessionKey) =>
          albums.name === name && albums.interpret === interpret && albums.userSessionKey === sessionKey
      }

    }.result)
  }

  def findAllUserAlbums() = {
    db.run(albumQuery.filter { albums =>
      identifier match {
        case Left(userId) =>
          albums.idUser === userId
        case Right(sessionKey) =>
          albums.userSessionKey === sessionKey
      }
    }.result)
  }

  def findAlbumsByArtist(artist:String) = {
    db.run(albumQuery.filter(_.interpret === artist).result)
  }

  def findAlbumsByName(album:String) = {
    db.run(albumQuery.filter(_.name === album).result)
  }
}
