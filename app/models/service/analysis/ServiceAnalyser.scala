package models.service.analysis

import models.database.facade.ArtistFacade
import models.database.facade.service._
import models.util.Constants
import models.util.ThreadPools.analysisExecutionContext

import scala.concurrent.Future

class ServiceAnalyser(identifier: Either[Int,String]) {

  val spotifyArtistFacade = SpotifyArtistFacade(identifier)
  val deezerArtistFacade = DeezerArtistFacade(identifier)
  val napsterArtistFacade = NapsterArtistFacade(identifier)
  val analyserList:List[ServiceAnalysisTrait] = List(SpotifyAnalysis, DeezerAnalysis, NapsterAnalysis)

  def analyse():Future[Boolean] = {
    val favouriteArtistsIds = ArtistFacade(identifier).mostListenedToArtists().take(Constants.maxArtistCountToAnalyse).map(_.key)
    val artists = ArtistFacade(identifier).usersFavouriteArtists(favouriteArtistsIds).map(_._1)
    val result:Future[List[Map[Long, List[(String, String, String)]]]] = Future.sequence {
      analyserList.map(_.apply(identifier, artists).analyse())
    }
    for {
      res <- result
      mergedMap = mergeMaps(res)((l1,l2) => l1 ++ l2)
      p <- persistData(mergedMap)
    } yield p.forall(_ == true)
   /* val spotifyResultFuture = SpotifyAnalysis(identifier, artists).analyse()
    val deezerResultFuture = DeezerAnalysis(identifier, artists).analyse()
    val napsterResultFuture = NapsterAnalysis(identifier, artists).analyse()
    for {
      spotifyResult <- spotifyResultFuture
      deezerResult <- deezerResultFuture
      napsterResult <- napsterResultFuture
      mergedMap:Map[Long, List[(String, String, String)]] = mergeMaps(List(spotifyResult, deezerResult, napsterResult))((l1,l2) => l1 ++ l2)
      p <- persistData(mergedMap)
    } yield p.forall(_ == true)*/
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
    val mapList = artistAlbumMap.toList
    Future.sequence {
      mapList.map { grp =>
        persistItem(grp)
      }
    }
  }

  private def persistItem(artistItem: (Long, List[(String, String, String)])):Future[Boolean] = {
    Future {
      val (artistDbId, albumList) = artistItem
      val groupedByService:Map[String, List[(String, String, String)]] = albumList.groupBy { case (_,_,service) => service}
      groupedByService.foreach { case (service, grpAlbList) =>
        val serviceArtistId:Long = serviceArtistFacade(service).saveArtist(artistDbId)
        grpAlbList.foreach { case (albumName,albumServiceId,_) =>
          serviceAlbumFacade(service).saveAlbumWithNameAndId(albumName, serviceArtistId, albumServiceId)
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
