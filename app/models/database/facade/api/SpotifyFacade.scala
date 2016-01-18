package models.database.facade.api

import scalikejdbc._

object SpotifyFacade extends ServiceFacade {
  val serviceFieldName = sqls"spotify_id"
}
