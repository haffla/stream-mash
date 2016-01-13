package models.database.alias

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

case class SpotifyAlbum(@Column("id_spotify_album") id:Long,
                        @Column("spotify_id") spotifyId:String)
