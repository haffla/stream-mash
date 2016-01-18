package models.database.alias.service

import org.squeryl.annotations._

case class NapsterArtist(@Column("id_napster_artist") id:Long) extends ServiceArtist {
  override def getId: Long = this.id
}
