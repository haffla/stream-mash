package models.database.alias

import org.squeryl.annotations._

case class DeezerAlbum(@Column("id_deezer_album") id:Long,
                       @Column("deezer_id") deezerId:String)
