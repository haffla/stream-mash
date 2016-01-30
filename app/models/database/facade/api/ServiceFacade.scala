package models.database.facade.api

import models.database.facade.Facade
import models.util.Constants
import play.api.Play.current
import play.api.cache.Cache
import scalikejdbc._

abstract class ServiceFacade extends Facade {

  val serviceFieldName:SQLSyntax
  val serviceArtistTable:Option[(SQLSyntax,SQLSyntax)] = None

  def saveArtistWithServiceId(artistName: String, serviceId: String, picUrl:Option[String] = None):Unit = {
    val cacheKey = s"$artistName|$serviceId|UPDATED"
    Cache.get(cacheKey) match {
      case Some(_) =>
      case None =>
        sql"select id_artist, $serviceFieldName from artist where artist_name=$artistName".map(
          rs => (rs.long("id_artist"), rs.string(serviceFieldName.value))
        ).single().apply() match {
          case Some((artistId, i)) =>
            if(i != serviceId) updateServiceIdAndServiceArtist(artistId, serviceId, picUrl)
          case None => createNewArtistWithId(artistName, serviceId, picUrl)
        }
        Cache.set(cacheKey, true)
    }
  }

  private def updateServiceIdAndServiceArtist(artistId: Long, serviceId: String, picUrl:Option[String]):Unit = {
    val sql = picUrl match {
      case Some(url) => sql"update artist set $serviceFieldName=$serviceId, pic_url=$url where id_artist=$artistId"
      case _ => sql"update artist set $serviceFieldName=$serviceId where id_artist=$artistId"
    }
    sql.update().apply()
    serviceArtistTable match {
      case Some(t) =>
        val table = t._1
        val field = t._2
        sql"select $field from $table where $field=$artistId".map(
          rs => rs.long(field.value)).single().apply()
        match {
          case None => insertNewServiceArtist(table, field, artistId)
          case _ =>
        }
      case _ =>
    }
  }

  def updateArtistsServiceId(artistId:Long, serviceId:String) = {
    sql"update artist set $serviceFieldName=$serviceId where id_artist=$artistId".update().apply()
  }

  private def createNewArtistWithId(artistName: String, serviceId: String, picUrl:Option[String]) = {
    val artistTableSql = picUrl match {
      case Some(url) => sql"insert into artist (artist_name, $serviceFieldName, pic_url) VALUES ($artistName, $serviceId, $picUrl)"
      case _ => sql"insert into artist (artist_name, $serviceFieldName) VALUES ($artistName, $serviceId)"
    }
    val artistDbId = artistTableSql.updateAndReturnGeneratedKey().apply()
    serviceArtistTable match {
      case Some(t) =>
        val table = t._1
        val field = t._2
        insertNewServiceArtist(table, field, artistDbId)
      case _ =>
    }
  }

  private def insertNewServiceArtist(table:SQLSyntax, field:SQLSyntax, artistDbId:Long) = {
    sql"insert into $table ($field, is_analysed) VALUES ($artistDbId, FALSE)".update().apply()
  }
}

object Services {

  def refreshTokenFieldForService(service:String) = {
    service match {
      case Constants.serviceSpotify => sqls"spotify_token_refresh"
      case Constants.serviceNapster => sqls"napster_token_refresh"
      case _ => throw new IllegalArgumentException("The given service '$service' has no refresh token field yet")
    }
  }

  def tokenFieldForService(service:String) = {
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
