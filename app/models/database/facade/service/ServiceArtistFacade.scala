package models.database.facade.service

import models.database.alias._
import models.database.facade.service.exporter.ServiceArtistExporter
import models.database.facade.{ServiceArtistAbsenceFacade, AlbumFacade, ArtistFacade}
import models.util.Constants
import org.squeryl.PrimitiveTypeMode._
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class ServiceArtistFacade(identifier:Either[Int,String]) {

  val serviceName:String
  val exporter = new ServiceArtistExporter(serviceName)

  def artistsAndAlbums(usersArtists:List[Long]):List[(Album,Artist,String)]

  def getArtistsAndAlbumsForOverview:Future[JsValue] = {
    val favouriteArtistsIds = ArtistFacade(identifier).mostListenedToArtists().take(Constants.maxArtistCountToAnalyse).map(_.key)
    for {
      albumsInUserCollection <- Future { AlbumFacade(identifier).getUsersFavouriteAlbums(favouriteArtistsIds) }
      serviceAlbums <- Future { getUserRelatedServiceAlbums(favouriteArtistsIds) }
      absentArtists <- Future { ServiceArtistAbsenceFacade.absentArtists(serviceName, favouriteArtistsIds)}
    } yield exporter.convertToJson(serviceAlbums,albumsInUserCollection,absentArtists)
  }

  /**
    * Get all service albums of those artists that were imported by the user
    */
  private def getUserRelatedServiceAlbums(mostListenedToArtists:List[Long]):List[(Album,Artist,String)] = {
    transaction {
      val usersArtists = ArtistFacade(identifier).usersFavouriteArtists(mostListenedToArtists).map(_._1.id)
      artistsAndAlbums(usersArtists)
    }
  }
}
