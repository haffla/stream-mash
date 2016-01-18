package models.database.facade.api

import scalikejdbc._

object SoundcloudFacade extends ServiceFacade {
  val serviceFieldName = sqls"soundcloud_id"
}
