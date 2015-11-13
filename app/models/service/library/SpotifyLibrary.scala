package models.service.library

import database.MainDatabaseAccess
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile

object SpotifyLibrary extends MainDatabaseAccess
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
        ArtistLibrary.createNewArtistWithSpotifyId(artist, spotifyId)
      }
    }
  }

}
