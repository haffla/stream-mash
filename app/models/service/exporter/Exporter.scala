package models.service.exporter

import models.database.alias._
import play.api.libs.json.{Json, JsObject, JsValue}

object Exporter {
  /**
    * Transforms the collection coming from the database to a Json Array of Json Objects
    */
  def prepareCollectionForFrontend(data:List[(Album,Artist,Track,UserCollection,UserArtistLiking,Long)]):JsValue = {
    val converted = convertToArtistMap(data)
    val jsObjects = converted.map { case (artistData,albumData) =>
      val (artistName,artistPic,artistRating,artistTrackCount) = artistData
      val albumObjects = albumData.map { album =>
        val albumName:String = album._1
        val tracks:Set[JsObject] = album._2.map { tr =>
          Json.obj(
            "name" -> tr._1,
            "played" -> tr._2
          )
        }
        Json.obj(
          "name" -> albumName,
          "tracks" -> tracks
        )
      }
      Json.obj(
        "name" -> artistName,
        "rating" -> artistRating,
        "img" -> artistPic,
        "albums" -> albumObjects,
        "trackCount" -> artistTrackCount
      )
    }
    Json.toJson(jsObjects)
  }

  private def convertToArtistMap(data:List[(Album,Artist,Track,UserCollection,UserArtistLiking,Long)]):Map[(String,String,Double,Long), Map[String,Set[(String,Int)]]] = {
    data.foldLeft(Map[(String,String,Double,Long), Map[String,Set[(String,Int)]]]()) { (prev, curr) =>
      val artistName = curr._2.name
      val artistPic = curr._2.pic.getOrElse("")
      val artistTrackCount = curr._6
      val userArtistRating = curr._5.score
      /*
       * The artist is now a 4-Tuple of Name:String,PictureUrl:String,Rating:Double,TrackCount:Long
       * Use this as the map key
      */
      val artist:(String,String,Double,Long) = (artistName,artistPic,userArtistRating,artistTrackCount)
      val album = curr._1.name
      // Track is a tuple of the name + times played
      val track:(String,Int) = (curr._3.name, curr._4.played)
      val albums:Map[String,Set[(String,Int)]] = prev.getOrElse(artist, Map.empty)
      val tracks:Set[(String,Int)] = albums.getOrElse(album, Set.empty) + track
      val added:Map[String,Set[(String,Int)]] = albums + (album -> tracks)
      prev + (artist -> added)
    }
  }
}
