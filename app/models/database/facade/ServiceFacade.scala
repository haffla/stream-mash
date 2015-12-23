package models.database.facade

import scalikejdbc._

abstract class ServiceFacade extends Facade {

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

object Services extends ServiceFacade {
  override val serviceFieldName = null

  def getFieldForService(service:String) = {
    service match {
      case "spotify" => sqls"spotify_token"
      case "rdio" => sqls"rdio_token"
      case "deezer" => sqls"deezer_token"
      case "soundcloud" => sqls"soundcloud_token"
      case "lastfm" => sqls"lastfm_token"
      case _ => throw new IllegalArgumentException("The given service '$service' is not supported")
    }
  }
}
