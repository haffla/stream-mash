package models.database.alias

import org.squeryl.KeyedEntity
import org.squeryl.annotations._
import org.squeryl.dsl.OneToMany

case class Artist(@Column("artist_name") name:String,
                  @Column("spotify_id") spotifyId:Option[String] = None,
                  @Column("soundcloud_id") soundcloudId:Option[String] = None,
                  @Column("deezer_id") deezerId:Option[String] = None,
                  @Column("lastfm_id") lastfmId:Option[String] = None) extends KeyedEntity[Long] {

  @Column("id_artist") val id:Long = 0

  lazy val albums:OneToMany[Album] = AppDB.artistToAlbums.left(this)
}
