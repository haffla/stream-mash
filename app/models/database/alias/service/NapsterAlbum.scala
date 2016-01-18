package models.database.alias.service

import org.squeryl.annotations._

case class NapsterAlbum(@Column("id_napster_album") id:Long,
                        @Column("napster_id") napsterId:String) extends ServiceAlbum {
  override def getId: Long = this.id
  override def serviceId: String = this.napsterId
}
