package models.service.analysis

import models.database.facade.ArtistFacade
import models.database.facade.service._
import models.service.Constants

import scala.concurrent.ExecutionContext.Implicits.global

class ServiceAnalyser(identifier: Either[Int,String]) {

  val spotifyArtistFacade = SpotifyArtistFacade(identifier)
  val deezerArtistFacade = DeezerArtistFacade(identifier)
  val napsterArtistFacade = NapsterArtistFacade(identifier)

  def analyse() = {
    val artists = ArtistFacade(identifier).usersFavouriteArtists()
    val spotifyResultFuture = SpotifyAnalysis(identifier, artists).analyse()
    val deezerResultFuture = DeezerAnalysis(identifier, artists).analyse()
    val napsterResultFuture = NapsterAnalysis(identifier, artists).analyse()
    for {
      spotifyResult <- spotifyResultFuture
      deezerResult <- deezerResultFuture
      napsterResult <- napsterResultFuture
    } yield {
      val mergedMap:Map[Long, List[(String, String, String)]] = mergeMaps(List(spotifyResult, deezerResult, napsterResult))((l1,l2) => l1 ++ l2)
      persistData(mergedMap)
      true
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

  private def persistData(artistAlbumMap: Map[Long, List[(String, String, String)]]) = {
    artistAlbumMap.foreach { case (artistDbId, albumList) =>
      val groupedByService:Map[String, List[(String, String, String)]] = albumList.groupBy { case (_,_,service) => service}
      groupedByService.foreach { case (service, grpAlbList) =>
        val serviceArtistId:Long = serviceArtistFacade(service).saveArtist(artistDbId)
          grpAlbList.foreach { case (albumName,albumServiceId,_) =>
            serviceAlbumFacade(service).saveAlbumWithNameAndId(albumName, serviceArtistId, albumServiceId)
          }
      }
    }
  }
}

object ServiceAnalyser {
  def apply(identifier: Either[Int,String]) = new ServiceAnalyser(identifier)
}
