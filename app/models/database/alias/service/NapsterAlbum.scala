package models.database.alias.service

import org.squeryl.annotations._

case class NapsterAlbum(@Column("id_napster_album") id:Long,
                        @Column("napster_id") napsterId:String)