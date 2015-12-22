package models.database.alias

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

case class Track(@Column("track_name") name:String,
                 @Column("fk_artist")  artistId:Long,
                 @Column("fk_album") albumId:Option[Long] = None) extends KeyedEntity[Long] {

  @Column("id_track") val id:Long = 0
}
