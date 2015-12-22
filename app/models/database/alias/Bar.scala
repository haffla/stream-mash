package models.database.alias

import org.squeryl.{Schema, KeyedEntity}
import org.squeryl.annotations.Column


case class Bar(name: Option[String]) extends KeyedEntity[Long] {
  val id: Long = 0
}

case class User(name:String,password:String,
                @Column("itunes_file_hash") itunesFileHash:Option[String],
                @Column("spotify_token") spotifyToken:Option[String],
                @Column("deezer_token") deezerToken:Option[String],
                @Column("rdio_token") rdioToken:Option[String],
                @Column("soundcloud_token") soundcloudToken:Option[String],
                @Column("lastfm_token") lastfmToken:Option[String]) extends KeyedEntity[Long] {
  @Column("id_user") val id: Long = 0
}

object AppDB extends Schema {
  val barTable = table[Bar]("bar")
  val userTable = table[User]("account")
}