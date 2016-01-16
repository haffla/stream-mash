package models.service.api

import scala.concurrent.Future

abstract class ApiFacade {
  lazy val ich = this.getClass.toString
}
