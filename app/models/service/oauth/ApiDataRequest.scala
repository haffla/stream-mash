package models.service.oauth

import models.service.api.discover.ApiHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class ApiDataRequest(name:String, identifier:Either[Int,String]) {
  val apiHelper = new ApiHelper(name, identifier)

  def requestUserData(code:String):Unit = {
    apiHelper.setRetrievalProcessPending()
    doDataRequest(code) map {_ => apiHelper.setRetrievalProcessDone()}
  }

  def doDataRequest(code:String):Future[Boolean]
}
