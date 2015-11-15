package database.facade

import database.MainDatabaseAccess
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import scalikejdbc._
import slick.driver.JdbcProfile

abstract class ServiceFacade extends MainDatabaseAccess with HasDatabaseConfig[JdbcProfile] {
  implicit val session = AutoSession
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val serviceFieldName:SQLSyntax

  def findArtistByName(name:String):List[Map[String,String]] = {
    val ordering = if (true) sqls"desc" else sqls"asc"
    sql"select * from artist where name=$name order by name ${ordering}"
      .toMap().list().apply()
      .map(_.mapValues(_.toString))
  }

  def saveArtistWithServiceId(artistName: String, serviceId: String): Unit = {
    val artistsByName = findArtistByName(artistName)
    artistsByName.headOption match {
      case Some(artist) =>
        val artistId = artist("id_artist").toInt
        artist.get(serviceFieldName.toString()) match {
          case Some(id) =>
            if(id != serviceId) {
              updateServiceId(artistId, serviceId)
            }
          case None =>
            updateServiceId(artistId, serviceId)
        }
      case None =>
        createNewArtistWithId(artistName, serviceId)
    }
  }

  def updateServiceId(artistId: Int, serviceId: String): Unit = {
    sql"update artist set ${serviceFieldName}=$serviceId where id_artist=$artistId".update().apply()
  }

  def createNewArtistWithId(artistName: String, serviceId: String) = {
    sql"insert into artist (name, ${serviceFieldName}) VALUES ($artistName, $serviceId)".update().apply()
  }
}
