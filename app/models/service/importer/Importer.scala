package models.service.importer

import models.database.facade._
import models.service.api.discover.RetrievalProcessMonitor
import models.util.Constants
import models.util.ThreadPools.importExecutionContext

import scala.concurrent.Future

class Importer(identifier: Either[Int, String], name:String = "baseimporter", persist:Boolean = true) {

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
        = curr._2.groupBy(_.getOrElse(keyAlbum, Constants.unknownAlbum))
      val albumsWithTracks = grpByAlbum.foldLeft(Map[String,Set[String]]()) { (p, c) =>
        val album = c._1
        val tracks = c._2.map(_(keyTrack)).toSet
        p + (album -> tracks)
      }
      prev + (artist -> albumsWithTracks)
    }
    if(persist) {
      persist(result).onComplete {
        case _ => apiHelper.setRetrievalProcessDone()
      }
    }
    result
  }

  private def persistItem(artistTuple: (String, Map[String, Set[String]])):Future[Boolean] = Future {
    val (artist,albums) = artistTuple
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

  def persist(collection: Map[String, Map[String,Set[String]]]):Future[List[Boolean]] = {
    val totalLength = collection.size
    val mapList = collection.toList
    Future.sequence {
      mapList.zipWithIndex.map { case (grp,i) =>
        apiHelper.setRetrievalProcessProgress(0.66 + (i+1) / totalLength / 3)
        persistItem(grp).recover {
          case e:Exception => false
        }
      }
    }
  }
}
