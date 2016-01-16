package models.service.api

import scala.concurrent.Future

abstract class ApiFacade {
  lazy val ich = this.getClass.toString
  def getArtistId(artist:String, token:Option[String] = None, recordAbsence:Boolean = false):Future[Option[(String,String)]]
}
