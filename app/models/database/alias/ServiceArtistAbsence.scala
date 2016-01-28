package models.database.alias

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

case class ServiceArtistAbsence(@Column("fk_artist") artistId:Long,
                                @Column("service") service:String) extends KeyedEntity[Long] {

  @Column("id_service_artist_absence") val id:Long = 0
}
