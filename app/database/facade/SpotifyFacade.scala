package database.facade

import models.service.SpotifyService
import scalikejdbc._

import scala.concurrent.Future

object SpotifyFacade extends ServiceFacade {

  def saveArtistId(artistName: String, spotifyId: String): Unit = {
    val artistsByName:List[Map[String,String]] = sql"select * from artist where name=$artistName"
      .toMap().list().apply()
      .map(_.mapValues(_.toString))
      
    artistsByName.headOption match {
      case Some(artist) =>
        val artistId = artist("id_artist")
        artist.get("spotify_id") match {
          case Some(id) =>
            if(id != spotifyId) {
              updateSpotifyId(artistId, spotifyId)
            }
          case None =>
            updateSpotifyId(artistName, spotifyId)
        }
      case None =>
        createNewArtistWithSpotifyId(artistName, spotifyId)
    }
  }

  def updateSpotifyId(artistId: String, spotifyId: String): Unit = {
    sql"update artist set spotify_id='$spotifyId' where id_artist=$artistId".update().apply()
  }

  def createNewArtistWithSpotifyId(artistName: String, id: String) = {
    sql"insert into artist (name, spotify_id) VALUES ($artistName, $id)".update().apply()
  }

  def getSpotifyIdForArtistFromDb(artist:String):Future[Option[String]] = {
    import driver.api._
    val id = for { a <- artistQuery if a.name === artist } yield a.spotifyId
    db.run(id.result.headOption)
  }

  def getSpotifyIdForArtistFromSpotify(artist: String): Future[Option[String]] = {
    SpotifyService.getArtistId(artist)
  }

}
