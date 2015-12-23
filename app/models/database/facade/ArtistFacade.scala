package models.database.facade

import models.database.alias.{AppDB, Artist}
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Session

object ArtistFacade {
  def apply(identifier:Either[Int,String]) = new ArtistFacade(identifier)
}

class ArtistFacade(identifier:Either[Int,String]) extends Facade {

  def getArtistByName(artistName:String):Option[Artist] = {
    transaction(AppDB.artists.where(a => a.name === artistName).headOption)
  }
}
