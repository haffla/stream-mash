package models.database.facade.service

import models.database.alias._
import models.database.facade.{ArtistFacade, AlbumFacade}
import models.util.Constants
import org.squeryl.PrimitiveTypeMode._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class ServiceArtistFacade(identifier:Either[Int,String]) {

  val serviceName:String

  protected def joinWithArtistsAndAlbums(usersArtists:List[Long]):List[(Album,Artist,String)]

  def getArtistsAndAlbumsForOverview:Future[JsValue] = {
    for {
      albumsInUserCollection <- Future { AlbumFacade(identifier).getUsersFavouriteAlbums }
      serviceAlbums <- Future { getUserRelatedServiceAlbums }
    } yield convertToJson(serviceAlbums,albumsInUserCollection)
  }

  /**
    * Get all service albums of those artists that were imported by the user
    */
  private def getUserRelatedServiceAlbums:List[(Album,Artist,String)] = {
    transaction {
      /** First get all related artists */
      val usersArtists =
        from(AppDB.artists, AppDB.collections, AppDB.tracks)((a,c,t) =>
          where(c.trackId === t.id and t.artistId === a.id and AppDB.userWhereClause(c,identifier))
            select a.id
        )
      joinWithArtistsAndAlbums(usersArtists.toList)
    }
  }

  private def convertToJson(albumCollection:List[(Album,Artist,String)], albumsInUserCollection:List[Album] = Nil):JsValue = {
    val convertedToMap = albumCollection.foldLeft(Map[Artist,Set[(String,String,Boolean)]]()) { (prev, curr) =>
      val artist = curr._2
      val currentAlbum = curr._1.name
      val albumServiceId = curr._3
      val userHasAlbumInCollection = albumsInUserCollection.contains(curr._1)
      val aggregatedAlbums = prev.getOrElse(artist, Set.empty) ++ Set((currentAlbum,albumServiceId,userHasAlbumInCollection))
      prev + (artist -> aggregatedAlbums)
    }
    doJsonConversion(convertedToMap,albumCollection.length,
                     albumsOnlyInUserCollection(albumCollection,albumsInUserCollection),
                     albumsInUserCollection.length)
  }

  private def albumsOnlyInUserCollection(albums:List[(Album,Artist,String)], albumsInUserCollection:List[Album]):List[JsValue] = {
    val serviceAlbums = albums.map(_._1)
    albumsInUserCollection.filter(alb => !serviceAlbums.contains(alb)).map { alb =>
      val art = ArtistFacade.artistById(alb.artistId)
      //TODO get album id from other services
      Json.obj(
        "artist" -> Json.obj(
          "name" -> art.name,
          "spotifyId" -> Json.toJson(art.spotifyId.getOrElse("")),
          "deezerId" -> Json.toJson(art.deezerId.getOrElse("")),
          "napsterId" -> Json.toJson(art.napsterId.getOrElse(""))
        ),
        "album" -> alb.name
      )
    }
  }

  private def doJsonConversion(artistAlbumMap: Map[Artist,Set[(String,String,Boolean)]],
                               nrAlbums:Int,
                               albumsOnlyInUserCollection:List[JsValue],
                               nrAlbumsInUserCollection:Int): JsValue = {
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
    Json.obj(
      Constants.jsonKeyArtists -> Json.toJson(list),
      Constants.jsonKeyStats -> Json.obj(
        Constants.jsonKeyNrAlbs -> nrAlbums,
        Constants.jsonKeyNrArts -> artistAlbumMap.size,
        Constants.jsonKeyNrUserAlbs -> nrAlbumsInUserCollection,
        Constants.jsonKeyAlbumsOnlyInUserCollection -> albumsOnlyInUserCollection
      )
    )
  }

  private def getServiceField(elem:Artist):String = {
    val field = serviceName match {
      case Constants.serviceSpotify => elem.spotifyId
      case Constants.serviceDeezer => elem.deezerId
      case Constants.serviceNapster => elem.napsterId
      case _ => throw new Exception("This service has not properly set up yet. Key not found.")
    }
    field.getOrElse("")
  }
}
