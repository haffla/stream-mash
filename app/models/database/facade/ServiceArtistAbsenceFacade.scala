package models.database.facade

import models.database.AppDB
import models.database.alias.ServiceArtistAbsence
import org.squeryl.PrimitiveTypeMode._

object ServiceArtistAbsenceFacade extends Facade {

  def insertIfNotExists(id:Long, service:String) = {
    inTransaction {
      from(AppDB.artists)(a =>
        where(a.id === id)
        select a.id
      ).headOption match {
        case Some(_) =>
          from(AppDB.serviceArtistAbsence)(saa =>
            where(saa.artistId === id and saa.service === service)
              select saa.id
          ).headOption match {
            case None =>
              AppDB.serviceArtistAbsence.insert(ServiceArtistAbsence(artistId = id, service = service))
            case _ =>
          }
        case _ =>
      }
    }
  }
}
