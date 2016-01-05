package models.database.alias

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

case class User(name:String,password:String,
                @Column("itunes_file_hash") itunesFileHash:Option[String],
                @Column("spotify_token") spotifyToken:Option[String],
                @Column("deezer_token") deezerToken:Option[String],
                @Column("soundcloud_token") soundcloudToken:Option[String],
                @Column("lastfm_token") lastfmToken:Option[String]) extends KeyedEntity[Long] {

  @Column("id_user") val id: Long = 0
}
