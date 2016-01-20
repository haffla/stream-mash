package models.service.analysis

import models.database.facade.ArtistFacade
import models.database.facade.service._

import scala.concurrent.ExecutionContext.Implicits.global

class ServiceAnalyser(identifier: Either[Int,String]) {

  val spotifyArtistFacade = SpotifyArtistFacade(identifier)
  val deezerArtistFacade = DeezerArtistFacade(identifier)
  val napsterArtistFacade = NapsterArtistFacade(identifier)

  def analyse() = {
    val artists = ArtistFacade(identifier).usersFavouriteArtists
    val spotifyResultFuture = SpotifyAnalysis(identifier, artists).analyse()
    val deezerResultFuture = DeezerAnalysis(identifier, artists).analyse()
    val napsterResultFuture = NapsterAnalysis(identifier, artists).analyse()
    for {
      spotifyResult <- spotifyResultFuture
      deezerResult <- deezerResultFuture
      napsterResult <- napsterResultFuture
    } yield {
      val resultList:List[(String, List[(String, String, String)])] = List(spotifyResult, deezerResult, napsterResult).flatMap(_.toList)
      val grouped:Map[String, List[(String, List[(String, String, String)])]] = resultList.groupBy { case(artist,albumList) => artist }
      persistData(grouped)
      true
    }
  }

  private def serviceArtistFacade(service:String):ServiceArtistTrait = {
    service match {
      case "spotify" => SpotifyArtistFacade
      case "deezer" => DeezerArtistFacade
      case "napster" => NapsterArtistFacade
      case _ => throw new Exception("Unknown service " + service)
    }
  }

  private def serviceAlbumFacade(service:String):ServiceAlbumFacade = {
    service match {
      case "spotify" => SpotifyAlbumFacade
      case "deezer" => DeezerAlbumFacade
      case "napster" => NapsterAlbumFacade
      case _ => throw new Exception("Unknown service " + service)
    }
  }

  private def persistData(artistAlbumMap: Map[String, List[(String, List[(String, String, String)])]]) = {
    artistAlbumMap.foreach { entity =>
      val artistName:String = entity._1
      val albums:List[(String, List[(String, String, String)])] = entity._2
      ArtistFacade.artistByName(artistName) match {
        case Some(artist) =>
          val artistId:Long = artist.id
          albums.foreach { alb =>
            val listOfAlbums:List[(String,String,String)] = alb._2
            val grouped = listOfAlbums.groupBy { case (art,id,service) => service}
            grouped.foreach { case (service, albumTupleList) =>
              val serviceArtistId:Long = serviceArtistFacade(service).saveArtist(artistId)
              albumTupleList.foreach { albumTuple =>
                val (albumName,albumId,_) = albumTuple
                serviceAlbumFacade(service).saveAlbumWithNameAndId(albumName,serviceArtistId,albumId)
              }
            }
          }
        case None =>
      }

    }
  }
}

object ServiceAnalyser {
  def apply(identifier: Either[Int,String]) = new ServiceAnalyser(identifier)
}
