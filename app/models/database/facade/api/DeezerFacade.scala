package models.database.facade.api

import scalikejdbc._

object DeezerFacade extends ServiceFacade {
  val serviceFieldName = sqls"deezer_id"
}
