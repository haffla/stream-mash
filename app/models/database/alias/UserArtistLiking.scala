package models.database.alias

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

case class UserArtistLiking(@Column("fk_user") userId:Option[Long] = None,
                            @Column("user_session") userSession:Option[String] = None,
                            @Column("fk_artist") artistId:Long,
                            @Column("score") score:Double) extends KeyedEntity[Long] {

  @Column("id_user_artist_liking") val id:Long = 0
}
