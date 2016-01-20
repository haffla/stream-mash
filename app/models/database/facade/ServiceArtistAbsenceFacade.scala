package models.database.facade

import models.database.alias.{ServiceArtistAbsence, Artist, AppDB}
import org.squeryl.PrimitiveTypeMode._

class ServiceArtistAbsenceFacade(identifier:Either[Int,String]) extends Facade {
  def save(artistName:String, service:String) = {
    transaction {
      getByArtistAndService(artistName, service) match {
        case Some(art) =>
          val absence = artistAbsenceByIdentifier(art.id, service)
          AppDB.serviceArtistAbsence.insert(absence)
        case _ =>
      }
    }
  }

  def getByArtistAndService(name:String, service:String):Option[Artist] = {
    from(AppDB.serviceArtistAbsence, AppDB.artists)((saa, a) =>
      where(a.name === name and a.id === saa.artistId and saa.service === service and AppDB.userWhereClause(saa, identifier))
        select a
    ).headOption
  }

  private def artistAbsenceByIdentifier(id:Long, service:String) = {
    identifier match {
      case Left(userId) => ServiceArtistAbsence(userId = Some(userId), artistId = id, service = service)
      case Right(userSession) => ServiceArtistAbsence(userSession = Some(userSession), artistId = id, service = service)
    }
  }

  def insertIfNotExists(id:Long, service:String) = {
    from(AppDB.serviceArtistAbsence)(saa =>
      where(saa.id === id and AppDB.userWhereClause(saa, identifier) and saa.service === service)
      select saa.id
    ).headOption match {
      case None =>
        val absence = artistAbsenceByIdentifier(id, service)
        AppDB.serviceArtistAbsence.insert(absence)
      case _ =>
    }
  }
}

object ServiceArtistAbsenceFacade {
  def apply(identifier:Either[Int,String]) = new ServiceArtistAbsenceFacade(identifier)
}
