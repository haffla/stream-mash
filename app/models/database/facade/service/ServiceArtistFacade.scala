package models.database.facade.service

import models.database.alias._
import models.database.facade.AlbumFacade
import org.squeryl.PrimitiveTypeMode._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class ServiceArtistFacade(identifier:Either[Int,String]) {

  val serviceName:String

  def joinWithArtistsAndAlbums(usersArtists:List[Long]):List[(Album,Artist,String)]

  def getArtistsAndAlbumsForOverview:Future[JsValue] = {
    for {
      albumsInUserCollection <- Future { AlbumFacade(identifier).getUsersAlbums }
      serviceAlbums <- Future { getUserRelatedServiceAlbums }
    } yield convertToJson(serviceAlbums,albumsInUserCollection)
  }

  /**
    * Get all service albums of those artists that were imported by the user
    */
  def getUserRelatedServiceAlbums:List[(Album,Artist,String)] = {
    transaction {

      /** First get all related artist */
      val usersArtists =
        from(AppDB.artists, AppDB.collections, AppDB.tracks)((a,c,t) =>
          where(c.trackId === t.id and t.artistId === a.id and AppDB.userWhereClause(c,identifier))
            select a.id
        )
      joinWithArtistsAndAlbums(usersArtists.toList)
    }
  }

  def convertToJson(albums:List[(Album,Artist,String)], albumsInUserCollection:List[Long] = Nil):JsValue = {
    val convertedToMap = albums.foldLeft(Map[Artist,Set[(String,String,Boolean)]]()) { (prev,curr) =>
      val artist = curr._2
      val currentAlbum = curr._1.name
      val albumServiceId = curr._3
      val userHasAlbumInCollection = albumsInUserCollection.contains(curr._1.id)
      val aggregatedAlbums = prev.getOrElse(artist, Set.empty) ++ Set((currentAlbum,albumServiceId,userHasAlbumInCollection))
      prev + (artist -> aggregatedAlbums)
    }
    doJsonConversion(convertedToMap)
  }

  private def doJsonConversion(artistAlbumMap: Map[Artist,Set[(String,String,Boolean)]]): JsValue = {
    val list = artistAlbumMap.map { elem =>
      val albums = elem._2.map { album =>
        Json.obj(
          "name" -> album._1,
          "id" -> album._2,
          "inCollection" -> album._3
        )
      }
      val serviceId:String = getServiceField(elem._1)
      val pic = elem._1.pic.getOrElse("")
      Json.obj(
        "name" -> elem._1.name,
        "id" -> serviceId,
        "img" -> pic,
        "albums" -> albums
      )
    }
    Json.toJson(list)
  }

  private def getServiceField(elem:Artist):String = {
    val field = serviceName match {
      case "spotify" => elem.spotifyId
      case "deezer" => elem.deezerId
      case "napster" => elem.napsterId
      case _ => throw new Exception("This service has not properly set up yet. Key not found.")
    }
    field.getOrElse("")
  }
}
