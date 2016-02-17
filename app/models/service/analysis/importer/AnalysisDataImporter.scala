package models.service.analysis.importer

import models.database.facade.service._
import models.util.Constants

import scala.concurrent.Future
import models.util.ThreadPools.analysisExecutionContext

object AnalysisDataImporter {

  def persist(data: List[Map[Long, List[(String, String, String)]]]):Future[List[Boolean]] = {
    val merged = mergeData(data)((l1,l2) => l1 ++ l2)
    persistData(merged)
  }

  private def mergeData[A,B](mapList: List[Map[B, List[A]]])(listOperation: (List[A], List[A]) => List[A]): Map[B, List[A]] = {

    /* This is the same, but maybe more readable.
    val tupleList:List[(B, List[A])] = for {
      m <- mapList
      kv <- m
    } yield {
      kv
    }
    tupleList.foldLeft(Map[B,List[A]]()) { (accu, kvPair) =>
      if(accu.contains(kvPair._1)) {
        accu + ( kvPair._1 -> listOperation(accu(kvPair._1), kvPair._2) )
      }
      else {
        accu + kvPair
      }
    }
    */
    (Map[B,List[A]]() /: (for (m <- mapList; kv <- m) yield kv)) { (accu, kvPair) =>
      accu + ( if(accu.contains(kvPair._1)) kvPair._1 -> listOperation(accu(kvPair._1), kvPair._2) else kvPair)
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

  private def persistItem(artistItem: (Long, List[(String, String, String)])):Future[Boolean] = Future {
      val (artistDbId, albumList) = artistItem
      val groupedByService:Map[String, List[(String, String, String)]] = albumList.groupBy { case (_,_,service) => service}
      groupedByService.foreach { case (service, grpAlbList) =>
        grpAlbList.foreach { case (albumName,albumServiceId,_) =>
          serviceAlbumFacade(service).saveAlbumWithNameAndId(albumName, artistDbId, albumServiceId)
        }
      }
      true
    }.recover {
      case e: Exception => false
    }
}
