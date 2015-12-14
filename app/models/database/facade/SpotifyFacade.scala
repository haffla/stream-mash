package models.database.facade

import scalikejdbc._

object SpotifyFacade extends ServiceFacade {
  val serviceFieldName = sqls"spotify_id"
}
