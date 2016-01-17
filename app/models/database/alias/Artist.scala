package models.database.alias

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

case class Artist(@Column("artist_name") name:String,
                  @Column("spotify_id") spotifyId:Option[String] = None,
                  @Column("napster_id") napsterId:Option[String] = None,
                  @Column("soundcloud_id") soundcloudId:Option[String] = None,
                  @Column("deezer_id") deezerId:Option[String] = None,
                  @Column("lastfm_id") lastfmId:Option[String] = None,
                  @Column("pic_url") pic:Option[String] = None) extends KeyedEntity[Long] {

  @Column("id_artist") val id:Long = 0

}
