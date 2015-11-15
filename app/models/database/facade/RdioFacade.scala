package models.database.facade

import scalikejdbc._

object RdioFacade extends ServiceFacade {
  val serviceFieldName = sqls"rdio_id"
}
