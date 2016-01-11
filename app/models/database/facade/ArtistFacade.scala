package models.database.facade

import models.database.alias.{AppDB, Artist}
import org.squeryl.PrimitiveTypeMode._

object ArtistFacade {
  def apply(identifier:Either[Int,String]) = new ArtistFacade(identifier)

  def getArtistByName(artistName:String):Option[Artist] = {
    transaction(AppDB.artists.where(a => a.name === artistName).headOption)
  }

  def getArtistPic(artistName:String):Option[String] = {
    getArtistByName(artistName) match {
      case Some(art) => art.pic
      case _ => None
    }
  }

  def setArtistPic(artistName:String, pic:String) = {
    transaction {
      update(AppDB.artists)(a =>
        where(a.name === artistName)
          set(a.pic := Some(pic))
      )
    }
  }
}

class ArtistFacade(identifier:Either[Int,String]) extends Facade {

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
