package models.database.facade

import models.database.alias.{AppDB, Artist}
import org.squeryl.PrimitiveTypeMode._

object ArtistFacade {
  def apply(identifier:Either[Int,String]) = new ArtistFacade(identifier)
}

class ArtistFacade(identifier:Either[Int,String]) extends Facade {

  def getArtistByName(artistName:String):Option[Artist] = {
    transaction(AppDB.artists.where(a => a.name === artistName).headOption)
  }

  def getUsersArtists:List[models.database.alias.Artist] = {
    transaction {
      val res = identifier match {
        case Left(id) =>
          from(AppDB.collections, AppDB.tracks, AppDB.artists)((coll, tr, art) =>
            where(coll.userId === id and coll.trackId === tr.id and tr.artistId === art.id)
            select art
          )
        case Right(s) =>
          from(AppDB.collections, AppDB.tracks, AppDB.artists)((coll, tr, art) =>
            where(coll.userSession === Some(s) and coll.trackId === tr.id and tr.artistId === art.id)
            select art
          )
      }
      res.distinct.toList
    }
  }
}
