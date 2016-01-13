package models.service.analysis

import models.database.facade.{ArtistFacade, AlbumFacade}
import models.service.util.ServiceAccessTokenHelper

abstract class ServiceAnalysis(identifier:Either[Int,String], service:String) {
  val serviceAccessTokenHelper = new ServiceAccessTokenHelper(service, identifier)
  val searchEndpoint:String
  val albumFacade:AlbumFacade
  val artistFacade:ArtistFacade
}
