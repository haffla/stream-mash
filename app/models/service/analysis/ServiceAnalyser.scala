package models.service.analysis

import models.database.facade.ArtistFacade
import models.database.facade.service._
import models.util.Constants

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ServiceAnalyser(identifier: Either[Int,String]) {

  val spotifyArtistFacade = SpotifyArtistFacade(identifier)
  val deezerArtistFacade = DeezerArtistFacade(identifier)
  val napsterArtistFacade = NapsterArtistFacade(identifier)

  def analyse():Future[Boolean] = {
    val artists = ArtistFacade(identifier).usersFavouriteArtists().map(_._1)
    val spotifyResultFuture = SpotifyAnalysis(identifier, artists).analyse()
    val deezerResultFuture = DeezerAnalysis(identifier, artists).analyse()
    val napsterResultFuture = NapsterAnalysis(identifier, artists).analyse()
    for {
      spotifyResult <- spotifyResultFuture
      deezerResult <- deezerResultFuture
      napsterResult <- napsterResultFuture
      mergedMap:Map[Long, List[(String, String, String)]] = mergeMaps(List(spotifyResult, deezerResult, napsterResult))((l1,l2) => l1 ++ l2)
      now = System.currentTimeMillis()
      p <- persistData(mergedMap)
    } yield {
      println("Took", (System.currentTimeMillis() - now) / 1000, "seconds")
      p.head
    }
  }

  private def mergeMaps[A,B](mapList: List[Map[B, List[A]]])(listOperation: (List[A], List[A]) => List[A]): Map[B, List[A]] = {
    (Map[B,List[A]]() /: (for (m <- mapList; kv <- m) yield kv)) { (a, kv) =>
      a + ( if(a.contains(kv._1)) kv._1 -> listOperation(a(kv._1), kv._2) else kv)
    }
  }

  private def serviceArtistFacade(service:String):ServiceArtistTrait = {
    service match {
      case Constants.serviceSpotify => SpotifyArtistFacade
      case Constants.serviceDeezer => DeezerArtistFacade
      case Constants.serviceNapster => NapsterArtistFacade
      case _ => throw new Exception("Unknown service " + service)
    }
  }

  private def serviceAlbumFacade(service:String):ServiceAlbumFacade = {
    service match {
      case Constants.serviceSpotify => SpotifyAlbumFacade
      case Constants.serviceDeezer => DeezerAlbumFacade
      case Constants.serviceNapster => NapsterAlbumFacade
      case _ => throw new Exception("Unknown service " + service)
    }
  }

  private def persistData(artistAlbumMap: Map[Long, List[(String, String, String)]]):Future[List[Boolean]] = {
    val av = Runtime.getRuntime.availableProcessors()
    val splitted = artistAlbumMap.grouped(artistAlbumMap.size / av).toList
    Future.sequence {
      splitted.map { grp =>
        doPersisting(grp)
      }
    }
  }

  private def doPersisting(artistAlbumMap: Map[Long, List[(String, String, String)]]):Future[Boolean] = {
    Future {
      artistAlbumMap.foreach { case (artistDbId, albumList) =>
        println(Thread.currentThread().getName)
        val groupedByService:Map[String, List[(String, String, String)]] = albumList.groupBy { case (_,_,service) => service}
        groupedByService.foreach { case (service, grpAlbList) =>
          val serviceArtistId:Long = serviceArtistFacade(service).saveArtist(artistDbId)
          grpAlbList.foreach { case (albumName,albumServiceId,_) =>
            serviceAlbumFacade(service).saveAlbumWithNameAndId(albumName, serviceArtistId, albumServiceId)
          }
        }
      }
      true
    }.recover {
      case e: Exception => false
    }
  }
}

object ServiceAnalyser {
  def apply(identifier: Either[Int,String]) = new ServiceAnalyser(identifier)
}
