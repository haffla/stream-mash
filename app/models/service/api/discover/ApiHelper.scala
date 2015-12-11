package models.service.api.discover

import models.auth.Helper
import org.joda.time.Hours
import play.api.cache.Cache
import play.api.Play.current
import scala.concurrent.duration._

class ApiHelper(service:String, identifier:Either[Int,String]) {

  val id = Helper.userIdentifierToString(identifier)

  private def setRetrievalProcess(flag: String) = {
    Cache.set(id + "|" + service, flag, Duration(1, HOURS))
  }

  def setRetrievalProcessDone() = setRetrievalProcess("done")
  def setRetrievalProcessPending() = setRetrievalProcess("pending")

  def getRetrievalProcessStatus = Cache.get(id + "|" + service)
}
