package models.database.facade

import scalikejdbc._

object LastfmFacade extends ServiceFacade {
  val serviceFieldName = sqls"lastfm_id"
}
