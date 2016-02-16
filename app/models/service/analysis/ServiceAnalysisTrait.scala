package models.service.analysis

import models.database.alias.Artist

trait ServiceAnalysisTrait {
  def apply(identifier:Either[Int,String], userFavouriteArtists: List[Artist]):ServiceAnalysis
}
