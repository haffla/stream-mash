package models.database.facade

import models.database.alias._
import org.squeryl.PrimitiveTypeMode._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CollectionFacade {
  def apply(identifier:Either[Int,String]) = new CollectionFacade(identifier)
}

class CollectionFacade(identifier:Either[Int,String]) extends Facade {

  def userCollection:Future[List[(Album,Artist,Track,UserCollection,Option[UserArtistLiking])]] = {
    Future {
      transaction {
        join(AppDB.artists, AppDB.tracks, AppDB.albums, AppDB.collections, AppDB.userArtistLikings.leftOuter)(
          (art,tr,alb,col,ual) =>
            where(AppDB.userWhereClause(col,identifier) and (AppDB.doesNotExist(ual) or AppDB.existsAndBelongsToUser(ual,identifier)))
            select (alb,art,tr,col,ual)
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

  def weightedArtists() = {
    transaction {
      from(AppDB.artists, AppDB.tracks, AppDB.collections)((a,t,c) =>
        where(a.id === t.artistId and t.id === c.trackId and AppDB.userWhereClause(c,identifier))
          groupBy a.id
          compute countDistinct(t.id)
      ).toList
    }
  }
}