package models.database.facade

import models.database.alias.AppDB
import org.squeryl.PrimitiveTypeMode._

object TrackFacade {
  def apply(identifier:Either[Int,String]) = new TrackFacade(identifier)
}

class TrackFacade(identifier:Either[Int,String]) extends Facade {
  def getUsersTracks:List[String] = {
    transaction {
      val res  = identifier match {
        case Left(userId) =>
          from(AppDB.collections, AppDB.tracks)((coll,tr) =>
            where(tr.id === coll.trackId and coll.userId === userId)
              select tr.name
          )
        case Right(session) =>
          from(AppDB.collections, AppDB.tracks)((coll,tr) =>
            where(tr.id === coll.trackId and coll.userSession === Some(session))
              select tr.name
          )
      }
      res.distinct.toList
    }
  }
}
