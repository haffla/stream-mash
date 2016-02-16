package models.service.analysis

import models.database.facade.ArtistFacade
import models.service.analysis.importer.AnalysisDataImporter
import models.util.Constants

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ServiceAnalyser(identifier: Either[Int,String]) {

  val analyserList:List[ServiceAnalysisTrait] = List(SpotifyAnalysis, DeezerAnalysis, NapsterAnalysis)

  def analyse():Future[Boolean] = {
    val favouriteArtistsIds = ArtistFacade(identifier).mostListenedToArtists().take(Constants.maxArtistCountToAnalyse).map(_.key)
    val artists = ArtistFacade(identifier).usersFavouriteArtists(favouriteArtistsIds).map(_._1)
    val result:Future[List[Map[Long, List[(String, String, String)]]]] = Future.sequence {
      analyserList.map(_.apply(identifier, artists).analyse())
    }
    for {
      res <- result
      p <- AnalysisDataImporter.persist(res)
    } yield p.forall(_ == true)
  }
}

object ServiceAnalyser {
  def apply(identifier: Either[Int,String]) = new ServiceAnalyser(identifier)
}
