package models.database.alias

import org.squeryl.KeyedEntity
import org.squeryl.annotations._
import org.squeryl.dsl.ManyToOne

case class Album(@Column("album_name") name:String,
                 @Column("fk_artist") artistId: Long) extends KeyedEntity[Long] {

  @Column("id_album") val id:Long = 0
  lazy val artist: ManyToOne[Artist] = AppDB.artistToAlbums.right(this)
}
