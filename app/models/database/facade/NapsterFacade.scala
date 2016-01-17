package models.database.facade

import scalikejdbc._

object NapsterFacade extends ServiceFacade {
  val serviceFieldName = sqls"napster_id"
}
