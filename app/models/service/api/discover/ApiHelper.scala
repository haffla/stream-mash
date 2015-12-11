package models.service.api.discover

import models.auth.Helper
import play.api.cache.Cache
import play.api.Play.current
import play.api.libs.iteratee.Concurrent
import scala.concurrent.duration._

class ApiHelper(service:String, identifier:Either[Int,String]) {

  val id = Helper.userIdentifierToString(identifier)

  private def setRetrievalProcess(flag: String) = {
    Cache.set(id + "|" + service, flag, Duration(1, HOURS))
  }

  def setRetrievalProcessDone() = setRetrievalProcess("done")
  def setRetrievalProcessPending() = setRetrievalProcess("pending")

  def getRetrievalProcessStatus = Cache.get(id + "|" + service)

  def retrievalProcessIsDone(channel:Concurrent.Channel[String], pollingTimeout:Int): Boolean = {
    getRetrievalProcessStatus match {
      case Some(status) =>
        channel push status.toString
        if(status == "done") true
        else {
          Thread.sleep(pollingTimeout)
          false
        }
      case None =>
        channel push "done"
        true
    }
  }
}
