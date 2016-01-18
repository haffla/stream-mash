package models.database.facade.api

import scalikejdbc._

object NapsterFacade extends ServiceFacade {
  val serviceFieldName = sqls"napster_id"
}
