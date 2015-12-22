package models.database.facade

import scalikejdbc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object ArtistFacade {
  def apply(identifier:Either[Int,String]) = new ArtistFacade(identifier)
}

class ArtistFacade(identifier:Either[Int,String]) extends Facade {

  def getArtistByName(artistName:String):Future[Option[Map[String, Any]]] = {
    Future {
      sql"select * from artist where artist_name = $artistName".toMap().single().apply()
    }
  }
}
