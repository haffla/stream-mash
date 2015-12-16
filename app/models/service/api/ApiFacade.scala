package models.service.api

import models.messaging.push.ArtistIdPusher

abstract class ApiFacade extends ArtistIdPusher {

  lazy val ich = this.getClass.toString
  val typ:String

  def pushToArtistIdQueue(artist:String, id:String):Unit = {
    pushToArtistIdQueue(artist, id, typ)
  }
}
