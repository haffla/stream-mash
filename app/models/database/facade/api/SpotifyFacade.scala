package models.database.facade.api

import scalikejdbc._

object SpotifyFacade extends ServiceFacade {
  val serviceFieldName = sqls"spotify_id"
  override val serviceArtistTable = Some((sqls"spotify_artist", sqls"id_spotify_artist"))
}
