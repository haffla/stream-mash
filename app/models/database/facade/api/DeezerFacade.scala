package models.database.facade.api

import scalikejdbc._

object DeezerFacade extends ServiceFacade {
  override val serviceFieldName = sqls"deezer_id"
  override val serviceArtistTable = Some((sqls"deezer_artist", sqls"id_deezer_artist"))
}
