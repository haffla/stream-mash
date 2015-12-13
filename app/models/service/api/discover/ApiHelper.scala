package models.service.api.discover

import models.auth.Helper
import play.api.cache.Cache
import play.api.Play.current
import play.api.libs.iteratee.Concurrent
import scala.concurrent.duration._

class ApiHelper(service:String, identifier:Either[Int,String]) {

  val id = Helper.userIdentifierToString(identifier)
  val serviceId = id + "|" + service
  val progressId = id + "|" + service + "|progress"

  private def setRetrievalProcess(flag: String) = Cache.set(serviceId, flag, Duration(1, HOURS))

  def setRetrievalProcessDone() = {
    setRetrievalProcess("done")
    Cache.remove(serviceId)
    Cache.remove(progressId)
  }
  def setRetrievalProcessPending() = setRetrievalProcess("pending")

  def getRetrievalProcessStatus = Cache.get(serviceId)

  def retrievalProcessIsDone(channel:Concurrent.Channel[String], pollingTimeout:Int): Boolean = {
    getRetrievalProcessStatus match {
      case Some(status) =>
        channel push status.toString
        getRetrievalProcessProgress match {
          case Some(progress) => channel push("progress:" + progress.toString)
          case None => channel push "progress:1"
        }
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

  def setRetrievalProcessProgress(progress:Double) = Cache.set(progressId, progress, Duration(1, HOURS))

  def getRetrievalProcessProgress = Cache.get(progressId)

}
