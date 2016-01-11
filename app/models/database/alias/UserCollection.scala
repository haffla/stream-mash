package models.database.alias

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

case class UserCollection(@Column("fk_user") userId:Option[Long],
                          @Column("fk_track") trackId:Long,
                          @Column("user_session") userSession:Option[String],
                          @Column("imported_from") importedFrom:Option[String]) extends KeyedEntity[Long] {

  @Column("id_collection") val id: Long = 0
}
