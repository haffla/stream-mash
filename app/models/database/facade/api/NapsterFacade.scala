package models.database.facade.api

import scalikejdbc._

object NapsterFacade extends ServiceFacade {
  val serviceFieldName = sqls"napster_id"
  override val serviceArtistTable = Some((sqls"napster_artist", sqls"id_napster_artist"))
}
