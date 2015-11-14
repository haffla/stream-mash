package database.facade

import database.MainDatabaseAccess
import database.alias.Artist
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global

object RdioFacade extends MainDatabaseAccess
                      with HasDatabaseConfig[JdbcProfile] {

  import driver.api._
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  def saveArtistId(artist: String, rdioId: String): Unit = {
    println(artist,rdioId)
    val artistsWithThatName = db.run(artistQuery.filter(_.name === artist).result)
    artistsWithThatName.map { artists =>
      if(artists.nonEmpty) {
        val existingId:String = artists.head.rdioId.orNull
        if(existingId != rdioId) {
          updateRdioId(artist, rdioId)
        }
      }
      else {
        createNewArtistWithRdioId(artist, rdioId)
      }
    }
  }

  private def updateRdioId(artist: String, rdioId: String): Unit = {
    //TODO: Make sure only one artist is updated
    val id = for { a <- artistQuery if a.name === artist } yield a.rdioId
    db.run(id.update(rdioId))
  }

  def createNewArtistWithRdioId(artist: String, rdioId: String) = {
    val newArtist = Artist(name = artist, rdioId = Some(rdioId))
    db.run(artistQuery += newArtist)
  }

}
