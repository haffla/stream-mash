package models.service.library

import models.database.alias._
import models.database.facade._
import models.service.api.discover.RetrievalProcessMonitor
import models.util.Constants
import play.api.libs.json.{JsObject, JsValue, Json}
import scalikejdbc._

import models.util.ThreadPools.importExecutionContext
import scala.concurrent.Future
import scala.util.{Failure, Success}

class Importer(identifier: Either[Int, String], name:String = "baseimporter", persist:Boolean = true) {

  implicit val session = AutoSession

  val apiHelper = new RetrievalProcessMonitor(name, identifier)
  val artistLikingFacade = ArtistLikingFacade(identifier)
  val collectionFacade = CollectionFacade(identifier)

  /**
   * Cleans the data by transforming the Seq[Map[String,String]]
   * to a Map[String, Map[String,Set[String]]]
   * The artist key maps to a map of which the keys are the album titles
   * and the values are sets of track names
   */
  def convertSeqToMap(data: Seq[Map[String,String]],
                      keyArtist:String = Constants.mapKeyArtist,
                      keyAlbum:String = Constants.mapKeyAlbum,
                      keyTrack:String = Constants.mapKeyTrack):Map[String, Map[String,Set[String]]] = {
    val grpByArtist:Map[String, Seq[Map[String, String]]] = data.groupBy(item => item(keyArtist))
    val result = grpByArtist.foldLeft(Map[String, Map[String,Set[String]]]()) { (prev, curr) =>
      val artist = curr._1
      val grpByAlbum:Map[String, Seq[Map[String, String]]]
        = curr._2.groupBy(_.getOrElse(keyAlbum, Constants.mapKeyUnknownAlbum))
      val albumsWithTracks = grpByAlbum.foldLeft(Map[String,Set[String]]()) { (p, c) =>
        val album = c._1
        val tracks = c._2.map(_(keyTrack)).toSet
        p + (album -> tracks)
      }
      prev + (artist -> albumsWithTracks)
    }
    if(persist) {
      persist(result).onComplete {
        case Success(a) =>
          apiHelper.setRetrievalProcessDone()
        case Failure(_) => apiHelper.setRetrievalProcessDone()
      }
    }
    result
  }

  /**
   * Transforms the collection coming from the database to a Json Array of Json Objects
   */
  def prepareCollectionForFrontend(data:List[(Album,Artist,Track,UserCollection,UserArtistLiking,Long)]):JsValue = {
    val converted = convert(data)
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

  private def convert(data:List[(Album,Artist,Track,UserCollection,UserArtistLiking,Long)]):Map[(String,String,Double,Long), Map[String,Set[(String,Int)]]] = {
    data.foldLeft(Map[(String,String,Double,Long), Map[String,Set[(String,Int)]]]()) { (prev, curr) =>
      val artistName = curr._2.name
      val artistPic = curr._2.pic.getOrElse("")
      val artistTrackCount = curr._6
      /**
        * If not rating is available it means the user has not rated the artist, we assume 1.0
        */
      val userArtistRating = curr._5.score
      /*
       * The artist is now a 3-Tuple of Name:String,PictureUrl:String,Rating:Double
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

  def persistItem(artists: (String, Map[String, Set[String]])):Future[Boolean] = Future {
    val (artist,albums) = artists
    val existingArtistId:Long = ArtistFacade.saveByName(artist, artistLikingFacade)
    for((album,tracks) <- albums) {
      val existingAlbumId:Long = AlbumFacade.saveByNameAndArtistId(album, existingArtistId)

      for(track <- tracks) {
        val trackId:Long = TrackFacade.saveTrack(track, existingArtistId, existingAlbumId)
        collectionFacade.save(trackId)
      }
    }
    true
  }

  def persist(library: Map[String, Map[String,Set[String]]]):Future[List[Boolean]] = {
    val totalLength = library.size
    val mapList = library.toList
    var position = 1.0
    Future.sequence {
      mapList.map { case grp =>
        apiHelper.setRetrievalProcessProgress(0.66 + position / totalLength / 3)
        position += 1.0
        persistItem(grp).recover {
          case e:Exception => false
        }
      }
    }
  }
}
