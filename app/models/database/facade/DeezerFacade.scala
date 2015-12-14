package models.database.facade

import scalikejdbc._

object DeezerFacade extends ServiceFacade {
  val serviceFieldName = sqls"deezer_id"
}
