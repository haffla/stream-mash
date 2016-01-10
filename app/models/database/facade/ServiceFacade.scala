package models.database.facade

import scalikejdbc._

abstract class ServiceFacade extends Facade {

  val serviceFieldName:SQLSyntax

  def saveArtistWithServiceId(artistName: String, serviceId: String, picUrl:String = ""): Unit = {
    sql"select artist_name, $serviceFieldName from artist where artist_name=$artistName".map(
        rs => (rs.string("artist_name"), rs.string(serviceFieldName.value))
    ).single().apply() match {
      case Some((artName, i)) =>
        if(i == serviceId) updateServiceId(artistName, serviceId, picUrl)
      case None => createNewArtistWithId(artistName, serviceId, picUrl)
    }
  }

  private def updateServiceId(artistName: String, serviceId: String, picUrl:String): Unit = {
    sql"update artist set $serviceFieldName=$serviceId, pic_url=$picUrl where artist_name=$artistName".update().apply()
  }

  private def createNewArtistWithId(artistName: String, serviceId: String, picUrl:String) = {
    sql"insert into artist (artist_name, $serviceFieldName, pic_url) VALUES ($artistName, $serviceId, $picUrl)".update().apply()
  }
}

object Services {

  def getFieldForService(service:String) = {
    service match {
      case "spotify" => sqls"spotify_token"
      case "deezer" => sqls"deezer_token"
      case "soundcloud" => sqls"soundcloud_token"
      case "lastfm" => sqls"lastfm_token"
      case _ => throw new IllegalArgumentException("The given service '$service' is not supported")
    }
  }
}
