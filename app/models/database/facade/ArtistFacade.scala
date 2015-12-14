package models.database.facade

import models.database.MainDatabaseAccess
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import scalikejdbc._
import slick.driver.JdbcProfile

import scala.concurrent.Future

object ArtistFacade {
  def apply(identifier:Either[Int,String]) = new ArtistFacade(identifier)
}

class ArtistFacade(identifier:Either[Int,String]) extends MainDatabaseAccess with HasDatabaseConfig[JdbcProfile] {

  import driver.api._
  implicit val session = AutoSession
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  def getArtistByName(artistName:String):Future[Option[models.database.alias.Artist]] = {
    val artist = for { a <- artistQuery if a.name === artistName } yield a
    db.run(artist.result.headOption)
  }
}
