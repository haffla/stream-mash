package models.util

import database.MainDatabaseAccess
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object ArtistLibrary extends MainDatabaseAccess
                     with HasDatabaseConfig[JdbcProfile] {

  import driver.api._
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  def createNewArtistWithSpotifyId(artist: String, id: String) = {
    val newArtist = models.music.Artist(name = artist, spotifyId = Some(id))
    db.run(artistQuery += newArtist)
  }

}
