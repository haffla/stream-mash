package models.service.oauth

import models.service.api.discover.RetrievalProcessMonitor
import models.service.importer.Importer
import models.service.util.ServiceAccessTokenHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class ApiDataRequest(name:String, identifier:Either[Int,String]) {
  val importer:Importer
  val apiHelper = new RetrievalProcessMonitor(name, identifier)
  val serviceAccessTokenHelper:ServiceAccessTokenHelper

  def apply(identifier: Either[Int,String]) = this
  def requestUserData(code:String):Unit = {
    apiHelper.setRetrievalProcessPending()
    doDataRequest(code) map { tokens =>
      val (accessToken, refreshToken) = tokens
      accessToken match {
        case Some(tkn) => serviceAccessTokenHelper.setAccessToken(tkn, refreshToken)
        case None =>
      }
    }
  }

  def doDataRequest(code:String):Future[(Option[String],Option[String])]
}
