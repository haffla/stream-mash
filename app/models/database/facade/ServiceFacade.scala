package models.database.facade

import models.database.MainDatabaseAccess
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import scalikejdbc._
import slick.driver.JdbcProfile

abstract class ServiceFacade extends MainDatabaseAccess with HasDatabaseConfig[JdbcProfile] {

  implicit val session = AutoSession
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val serviceFieldName:SQLSyntax

  def saveArtistWithServiceId(artistName: String, serviceId: String): Unit = {
    sql"select $serviceFieldName from artist where artist_name=$artistName".map(rs => rs.string(serviceFieldName.value)).single().apply() match {
      case Some(id) =>
        if(id != serviceId) {
          updateServiceId(artistName, serviceId)
        }
      case None => createNewArtistWithId(artistName, serviceId)
    }
  }

  private def updateServiceId(artistName: String, serviceId: String): Unit = {
    sql"update artist set $serviceFieldName=$serviceId where artist_name=$artistName".update().apply()
  }

  private def createNewArtistWithId(artistName: String, serviceId: String) = {
    sql"insert into artist (artist_name, $serviceFieldName) VALUES ($artistName, $serviceId)".update().apply()
  }
}
