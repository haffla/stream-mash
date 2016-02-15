package models.database.alias

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

case class UserCollection(@Column("fk_user") userId:Option[Long] = None,
                          @Column("fk_track") trackId:Long,
                          @Column("user_session") userSession:Option[String] = None) extends KeyedEntity[Long] with HasUserOrSession {

  @Column("id_collection") val id: Long = 0
  override def getUserId:Option[Long] = this.userId
  override def getUserSession:Option[String] = this.userSession
}
