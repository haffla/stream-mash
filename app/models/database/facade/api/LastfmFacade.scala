package models.database.facade.api

import scalikejdbc._

object LastfmFacade extends ServiceFacade {
  val serviceFieldName = sqls"lastfm_id"
}
