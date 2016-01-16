package models.service.api

import scala.concurrent.Future

abstract class ApiFacade {
  lazy val ich = this.getClass.toString
  def getArtistId(artist:String, recordAbsence:Boolean = false):Future[Option[(String,String)]]
}
