package models.database.facade

import models.database.alias.AppDB
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.ast.LogicalBoolean


class ServiceArtistAbsenceFacade(identifier:Either[Int,String]) extends Facade {
  def insert(artistName:String, service:String) = {

  }

  def getByArtistAndService(name:String, service:String) = {
    from(AppDB.serviceArtistAbsence, AppDB.artists)((saa, a) =>
      where(a.name === name and a.id === saa.artistId and saa.service === service and AppDB.userWhereClause(saa, identifier))
        select a
    )
  }
}
