package models.database.alias

import org.squeryl.KeyedEntity
import org.squeryl.annotations._

case class SpotifyArtist(@Column("id_spotify_artist") id:Long)
