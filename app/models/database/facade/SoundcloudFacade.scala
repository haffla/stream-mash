package models.database.facade

import scalikejdbc._

object SoundcloudFacade extends ServiceFacade {
  val serviceFieldName = sqls"soundcloud_id"
}
