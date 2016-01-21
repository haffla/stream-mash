package models.database.facade

import models.database.alias._
import org.squeryl.PrimitiveTypeMode
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.GroupWithMeasures

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CollectionFacade {
  def apply(identifier:Either[Int,String]) = new CollectionFacade(identifier)
}

class CollectionFacade(identifier:Either[Int,String]) extends Facade {

  def userCollection:Future[List[(Album,Artist,Track,UserCollection,Option[UserArtistLiking],Long)]] = {
    Future {
      transaction {
        val weighted = weightedArtists()
        // Use this list to filter in the where clause
        val weightedArtistIds:List[Long] = weighted.map(_.key)
        // This map is used then in the select statement
        val artistTrackCountMap:Map[Long,Long] = weighted.foldLeft(Map[Long,Long]()) { (prev,curr) =>
          prev + (curr.key -> curr.measures)
        }
        join(AppDB.artists, AppDB.tracks, AppDB.albums, AppDB.collections, AppDB.userArtistLikings.leftOuter)(
          (art,tr,alb,col,ual) =>
            where(
              AppDB.userWhereClause(col,identifier) and
              (AppDB.doesNotExist(ual) or
               AppDB.existsAndBelongsToUser(ual,identifier)
              ) and art.id.in(weightedArtistIds)
            )
            select (alb,art,tr,col,ual,artistTrackCountMap.getOrElse(art.id, 0L))
            on(
              tr.artistId === art.id,
              tr.albumId === alb.id,
              col.trackId === tr.id,
              art.id === ual.map(_.artistId)
              )
          ).toList
      }
    }
  }

  def weightedArtists():List[GroupWithMeasures[Long, Long]] = {
    from(AppDB.artists, AppDB.tracks, AppDB.collections)((a,t,c) =>
      where(a.id === t.artistId and t.id === c.trackId and AppDB.userWhereClause(c,identifier))
        groupBy a.id
        compute countDistinct(t.id)
    ).toList.sortBy(_.measures)(Ordering[PrimitiveTypeMode.LongType].reverse)
  }
}