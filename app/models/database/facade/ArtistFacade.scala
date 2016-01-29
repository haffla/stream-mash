package models.database.facade

import models.database.alias.{AppDB, Artist}
import models.util.GroupMeasureConversion
import org.squeryl.PrimitiveTypeMode
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.GroupWithMeasures

object ArtistFacade {
  def saveByName(artist: String, artistLikingFacade: ArtistLikingFacade, score:Double = 1):Long = {
    inTransaction {
      byName(artist) match {
        case Some(art) =>
          artistLikingFacade.insertIfNotExists(art.id, score)
          art.id
        case _ => insert(artist, artistLikingFacade, score)
      }
    }
  }

  def insert(name: String, artistLikingFacade: ArtistLikingFacade, score:Double = 1):Long = {
    val artistDbId = AppDB.artists.insert(Artist(name)).id
    artistLikingFacade.insert(artistDbId, score)
    artistDbId
  }

  private def byName(artist:String) = {
    AppDB.artists.where(a => a.name === artist).headOption
  }

  def apply(identifier:Either[Int,String]) = new ArtistFacade(identifier)

  def artistByName(artistName:String):Option[Artist] = {
    transaction(byName(artistName))
  }

  def artistById(artistId:Long):Artist = {
    transaction(AppDB.artists.where(a => a.id === artistId).single)
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

class ArtistFacade(identifier:Either[Int,String]) extends Facade with GroupMeasureConversion {

  /**
    * Get users favourite artists sorted by score in user_artist_liking table
    * By default get top 50.
    */
  def usersFavouriteArtists(page: (Int,Int) = (0,50)):List[(Artist,Double)] = {
    inTransaction {
      join(
        AppDB.artists,
        AppDB.tracks,
        AppDB.collections,
        AppDB.userArtistLikings)((a,tr,col,ual) =>
        where(ual.score.gt(0) and AppDB.userWhereClause(ual,identifier) and AppDB.userWhereClause(col,identifier))
          select(a,ual.score)
          orderBy ual.score.desc
          on(
            a.id === tr.artistId,
            col.trackId === tr.id,
            ual.artistId === a.id
          )
      ).distinct.page(page._1,page._2).toList
    }
  }

  def usersFavouriteArtistsWithTrackCountAndScore(page: (Int,Int) = (0,25)):List[(Artist,Long,Double)] = {
    inTransaction {
      val mlta = toMap(mostListenedToArtists())
      join(
        AppDB.artists,
        AppDB.tracks,
        AppDB.collections,
        AppDB.userArtistLikings)((a,tr,col,ual) =>
        where(ual.score.gt(0) and AppDB.userWhereClause(ual,identifier) and AppDB.userWhereClause(col,identifier))
          select(a,mlta.getOrElse(a.id, 1L),ual.score)
          orderBy ual.score.desc
          on(
          a.id === tr.artistId,
          col.trackId === tr.id,
          ual.artistId === a.id
          )
      ).distinct.page(page._1,page._2).toList
    }
  }

  def mostListenedToArtists():List[GroupWithMeasures[Long, Long]] = {
    from(AppDB.artists, AppDB.tracks, AppDB.collections)((a,t,c) =>
      where(a.id === t.artistId and t.id === c.trackId and AppDB.userWhereClause(c,identifier))
        groupBy a.id
        compute countDistinct(t.id)
    ).toList.sortBy(_.measures)(Ordering[PrimitiveTypeMode.LongType].reverse)
  }
}
