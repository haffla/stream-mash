package models.database.alias

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

case class UserArtistLiking(@Column("fk_user") userId:Option[Long] = None,
                            @Column("user_session") userSession:Option[String] = None,
                            @Column("fk_artist") artistId:Long,
                            @Column("score") score:Double) extends KeyedEntity[Long] with HasUserOrSession with OuterJoinedArtistRelatedEntity {

  @Column("id_user_artist_liking") val id:Long = 0
  override def getUserId: Option[Long] = this.userId
  override def getUserSession: Option[String] = this.userSession
  override def getArtistId: Long = this.artistId
}
