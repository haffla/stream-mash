package models.database.facade

import scalikejdbc._

import scala.concurrent.Future

object SpotifyFacade extends ServiceFacade {

  val serviceFieldName = sqls"spotify_id"

  def getSpotifyIdForArtistFromDb(artist:String):Future[Option[String]] = {
    import driver.api._
    val id = for { a <- artistQuery if a.name === artist } yield a.spotifyId
    db.run(id.result.headOption)
  }
}
