package models.database.facade

import models.database.alias.AppDB

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CollectionFacade {
  def apply(identifier:Either[Int,String]) = new CollectionFacade(identifier)
}

class CollectionFacade(identifier:Either[Int,String]) extends Facade {

  def userCollection:Future[List[(models.database.alias.Album, models.database.alias.Artist)]] = {
    Future {
      AppDB.getCollectionByUser(identifier)
    }
  }
}