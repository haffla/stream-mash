package models.database.alias.service

import org.squeryl.annotations._

case class SpotifyAlbum(@Column("id_spotify_album") id:Long,
                        @Column("spotify_id") spotifyId:String) extends ServiceAlbum {

  override def getId: Long = this.id
  override def serviceId: String = this.spotifyId
}
