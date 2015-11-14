package database.facade

import database.MainDatabaseAccess
import database.alias.Artist
import models.service.SpotifyService
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile

import scala.concurrent.Future

object SpotifyFacade extends MainDatabaseAccess
                      with HasDatabaseConfig[JdbcProfile] {

  import driver.api._
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  private def updateSpotifyId(artist: String, spotifyId: String): Unit = {
    //TODO: Make sure only one artist is updated
    val id = for { a <- artistQuery if a.name === artist } yield a.spotifyId
    db.run(id.update(spotifyId))
  }

  def saveArtistId(artist: String, spotifyId: String): Unit = {
    val artistsWithThatName = db.run(artistQuery.filter(_.name === artist).result)
    artistsWithThatName.map { artists =>
      if(artists.nonEmpty) {
        val existingId:String = artists.head.spotifyId.orNull
        if(existingId != spotifyId) {
          updateSpotifyId(artist, spotifyId)
        }
      }
      else {
        createNewArtistWithSpotifyId(artist, spotifyId)
      }
    }
  }

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
