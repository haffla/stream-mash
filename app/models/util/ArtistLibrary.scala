package models.util

import database.MainDatabaseAccess
import models.service.SpotifyService
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object ArtistLibrary extends MainDatabaseAccess
                     with HasDatabaseConfig[JdbcProfile] {

  import driver.api._
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  def createNewArtistWithSpotifyId(artist: String, id: String) = {
    val newArtist = models.music.Artist(name = artist, spotifyId = Some(id))
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
