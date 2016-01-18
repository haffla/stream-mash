package models.service.analysis

import models.database.facade.ArtistFacade

import scala.concurrent.ExecutionContext.Implicits.global

class ServiceAnalyser(identifier: Either[Int,String]) {

  def analyse() = {
    val artists = ArtistFacade(identifier).usersFavouriteArtists
    for {
      spotifyResult <- SpotifyAnalysis(identifier, artists).analyse()
      deezerResult <- DeezerAnalysis(identifier, artists).analyse()
      napsterResult <- NapsterAnalysis(identifier, artists).analyse()
    } yield List(spotifyResult, deezerResult, napsterResult).forall(_ == true)
  }
}

object ServiceAnalyser {
  def apply(identifier: Either[Int,String]) = new ServiceAnalyser(identifier)
}
