package models.database.facade

import models.database.alias.{Album,AppDB,Artist,Track,UserCollection}
import org.squeryl.PrimitiveTypeMode._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CollectionFacade {
  def apply(identifier:Either[Int,String]) = new CollectionFacade(identifier)
}

class CollectionFacade(identifier:Either[Int,String]) extends Facade {

  def userCollection:Future[List[(Album, Artist, Track, UserCollection)]] = {
    Future {
      transaction {
        from(AppDB.collections, AppDB.tracks, AppDB.albums, AppDB.artists)((coll, tr, alb, art) =>
          where(AppDB.userWhereClause(coll, identifier) and coll.trackId === tr.id and tr.albumId === alb.id and tr.artistId === art.id)
            select (alb,art,tr,coll)
        ).toList
      }
    }
  }
}