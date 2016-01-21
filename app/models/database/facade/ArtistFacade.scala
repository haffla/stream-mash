package models.database.facade

import models.database.alias.{AppDB, Artist}
import org.squeryl.PrimitiveTypeMode._

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

  /**
    * Get users favourite artists sorted by score in user_artist_liking table
    * By default get top 50.
    */
  def usersFavouriteArtists(page: (Int,Int) = (0,50)):List[Artist] = {
    transaction {
      join(
        AppDB.artists,
        AppDB.tracks,
        AppDB.collections,
        AppDB.userArtistLikings.leftOuter)((a,tr,col,ual) =>
        where((ual.map(_.score).isNull or ual.map(_.score).gt(0)) and AppDB.userWhereClause(col,identifier))
          select a
          orderBy ual.map(_.score).desc
          on(
          a.id === tr.artistId,
          col.trackId === tr.id,
          ual.map(_.artistId) === a.id
          )
      ).page(page._1,page._2).toList
    }
  }
}
