package database.facade

import database.MainDatabaseAccess
import database.alias.Artist
import models.service.SpotifyService
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.driver.JdbcProfile

import scala.concurrent.Future

object artistFacade extends MainDatabaseAccess
                     with HasDatabaseConfig[JdbcProfile] {

  import driver.api._
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  def createNewArtistWithSpotifyId(artist: String, id: String) = {
    val newArtist = Artist(name = artist, spotifyId = Some(id))
    db.run(artistQuery += newArtist)
  }

  def getSpotifyIdForArtistFromDb(artist:String):Future[Option[String]] = {
    val id = for { a <- artistQuery if a.name === artist } yield a.spotifyId
    db.run(id.result.headOption)
  }

  def getSpotifyIdForArtistFromSpotify(artist: String): Future[Option[String]] = {
    SpotifyService.getArtistId(artist)
  }
}
