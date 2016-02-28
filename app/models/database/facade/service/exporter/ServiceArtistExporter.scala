package models.database.facade.service.exporter

import models.database.alias.{Artist, Album}
import models.database.facade.ArtistFacade
import models.util.Constants
import play.api.libs.json.{Json, JsValue}

class ServiceArtistExporter(serviceName: String) {

  def convertToJson(albumCollection:List[(Album,Artist,String)],
                    albumsInUserCollection:List[Album],
                    absentArtists:List[Artist]): JsValue = {
    val convertedToMap = albumCollection.foldLeft(Map[Artist,List[(String,String,Boolean)]]()) { (prev, curr) =>
      val artist = curr._2
      val currentAlbum = curr._1.name
      val albumServiceId = curr._3
      val userHasAlbumInCollection = albumsInUserCollection.contains(curr._1)
      val aggregatedAlbums = prev.getOrElse(artist, Set.empty) ++ Set((currentAlbum,albumServiceId,userHasAlbumInCollection))
      prev + (artist -> aggregatedAlbums.toList.sortBy(_._1))
    }
    doJsonConversion(convertedToMap,albumCollection.length,
      albumsOnlyInUserCollection(albumCollection,albumsInUserCollection),
      absentArtistsToJson(absentArtists),
      albumsInUserCollection.length)
  }

  private def absentArtistsToJson(absentArtists:List[Artist]):List[JsValue] = {
    absentArtists.map { art =>
      Json.obj(
        "name" -> art.name,
        "spotifyId" -> Json.toJson(art.spotifyId.getOrElse("")),
        "deezerId" -> Json.toJson(art.deezerId.getOrElse("")),
        "napsterId" -> Json.toJson(art.napsterId.getOrElse(""))
      )
    }
  }

  private def albumsOnlyInUserCollection(albums:List[(Album,Artist,String)], albumsInUserCollection:List[Album]):List[JsValue] = {
    val serviceAlbums = albums.map(_._1)
    albumsInUserCollection.filter(alb => !serviceAlbums.contains(alb) && alb.name != Constants.unknownAlbum).map { alb =>
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

  private def doJsonConversion(artistAlbumMap: Map[Artist,List[(String,String,Boolean)]],
                               nrAlbums:Int,
                               albumsOnlyInUserCollection:List[JsValue],
                               absentArtist:List[JsValue],
                               nrAlbumsInUserCollection:Int): JsValue = {
    val list = artistAlbumMap.map { elem =>
      val x = elem._2
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
        Constants.jsonKeyAlbumsOnlyInUserCollection -> albumsOnlyInUserCollection,
        Constants.absentArtists -> absentArtist
      )
    )
  }

  private def getServiceField(elem:Artist):String = {
    val field = serviceName match {
      case Constants.serviceSpotify => elem.spotifyId
      case Constants.serviceDeezer => elem.deezerId
      case Constants.serviceNapster => elem.napsterId
      case _ => throw new Exception("This service has not been set up properly yet. Key not found.")
    }
    field.getOrElse("")
  }
}
