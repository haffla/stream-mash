package models.database.facade

import models.database.alias.{AppDB, Artist}
import org.squeryl.PrimitiveTypeMode
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.GroupWithMeasures

object ArtistFacade {
  def apply(identifier:Either[Int,String]) = new ArtistFacade(identifier)

  def artistByName(artistName:String):Option[Artist] = {
    transaction(AppDB.artists.where(a => a.name === artistName).headOption)
  }

  def artistPic(artistName:String):Option[String] = {
    artistByName(artistName) match {
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

  def usersArtists:List[Artist] = {
    transaction {
      from(AppDB.collections, AppDB.tracks, AppDB.artists)((coll, tr, art) =>
        where(AppDB.userWhereClause(coll,identifier) and coll.trackId === tr.id and tr.artistId === art.id)
        select art
      ).distinct.toList
    }
  }

  def usersFavouriteArtists:List[Artist] = {
    transaction {
      join(
        AppDB.artists,
        AppDB.tracks,
        AppDB.collections,
        AppDB.userArtistLikings.leftOuter)((a,tr,col,ual) =>
        where((ual.map(_.score).isNull or ual.map(_.score).gt(0)) and AppDB.userWhereClause(col,identifier))
          select a
          on(
          a.id === tr.artistId,
          col.trackId === tr.id,
          ual.map(_.artistId) === a.id
          )
      ).toList
    }
  }
}
