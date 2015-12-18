package models.service.oauth

import models.service.api.discover.RetrievalProcessMonitor
import models.service.util.ServiceAccessTokenCache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class ApiDataRequest(name:String, identifier:Either[Int,String]) {
  val apiHelper = new RetrievalProcessMonitor(name, identifier)
  val serviceAccessTokenCache:ServiceAccessTokenCache

  def requestUserData(code:String):Unit = {
    apiHelper.setRetrievalProcessPending()
    doDataRequest(code) map {token =>
      apiHelper.setRetrievalProcessDone()
      token match {
        case Some(tkn) => serviceAccessTokenCache.setAccessToken(tkn)
        case None =>
      }

    }
  }

  def doDataRequest(code:String):Future[Option[String]]
}
