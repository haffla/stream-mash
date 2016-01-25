package models.database.facade.api

import models.database.facade.Facade
import models.util.Constants
import play.api.Play.current
import play.api.cache.Cache
import scalikejdbc._

abstract class ServiceFacade extends Facade {

  val serviceFieldName:SQLSyntax

  def saveArtistWithServiceId(artistName: String, serviceId: String, picUrl:Option[String] = None):Unit = {
    val cacheKey = s"$artistName|$serviceId|UPDATED"
    Cache.get(cacheKey) match {
      case Some(_) =>
      case None =>
        sql"select artist_name, $serviceFieldName from artist where artist_name=$artistName".map(
          rs => (rs.string("artist_name"), rs.string(serviceFieldName.value))
        ).single().apply() match {
          case Some((artName, i)) =>
            if(i != serviceId) updateServiceId(artistName, serviceId, picUrl)
          case None => createNewArtistWithId(artistName, serviceId, picUrl)
        }
        Cache.set(cacheKey, true)
    }
  }

  private def updateServiceId(artistName: String, serviceId: String, picUrl:Option[String]):Unit = {
    val sql = picUrl match {
      case Some(url) => sql"update artist set $serviceFieldName=$serviceId, pic_url=$url where artist_name=$artistName"
      case _ => sql"update artist set $serviceFieldName=$serviceId where artist_name=$artistName"
    }
    sql.update().apply()
  }

  def updateArtistsServiceId(artistId:Long, serviceId:String) = {
    sql"update artist set $serviceFieldName=$serviceId where id_artist=$artistId".update().apply()
  }

  private def createNewArtistWithId(artistName: String, serviceId: String, picUrl:Option[String]) = {
    val sql = picUrl match {
      case Some(url) => sql"insert into artist (artist_name, $serviceFieldName, pic_url) VALUES ($artistName, $serviceId, $picUrl)"
      case _ => sql"insert into artist (artist_name, $serviceFieldName) VALUES ($artistName, $serviceId)"
    }
    sql.update().apply()
  }
}

object Services {

  def getRefreshFieldForService(service:String) = {
    service match {
      case Constants.serviceSpotify => sqls"spotify_token_refresh"
      case Constants.serviceNapster => sqls"napster_token_refresh"
      case _ => throw new IllegalArgumentException("The given service '$service' has no refresh token field yet")
    }
  }

  def getFieldForService(service:String) = {
    service match {
      case Constants.serviceSpotify => sqls"spotify_token"
      case Constants.serviceDeezer => sqls"deezer_token"
      case Constants.serviceSoundcloud => sqls"soundcloud_token"
      case Constants.serviceLastFm => sqls"lastfm_token"
      case Constants.serviceNapster => sqls"napster_token"
      case _ => throw new IllegalArgumentException("The given service '$service' is not supported")
    }
  }
}
