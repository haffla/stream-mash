package models.database.facade

import models.database.alias.AppDB
import org.squeryl.PrimitiveTypeMode._

object TrackFacade {
  def apply(identifier:Either[Int,String]) = new TrackFacade(identifier)
}

class TrackFacade(identifier:Either[Int,String]) extends Facade {
  def getUsersTracks:List[String] = {
    transaction {
      from(AppDB.collections, AppDB.tracks)((coll,tr) =>
        where(tr.id === coll.trackId and AppDB.userWhereClause(coll,identifier))
          select tr.name
      ).distinct.toList
    }
  }
}
