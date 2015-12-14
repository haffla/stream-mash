package models.service.api.discover

import models.auth.Helper
import play.api.cache.Cache
import play.api.Play.current
import play.api.libs.iteratee.Concurrent
import scala.concurrent.duration._

class RetrievalProcessMonitor(service:String, identifier:Either[Int,String]) {

  val id = Helper.userIdentifierToString(identifier)
  val serviceId = id + "|" + service
  val progressId = serviceId + "|progress"

  private def setRetrievalProcess(flag: String) = Cache.set(serviceId, flag, Duration(1, HOURS))

  def setRetrievalProcessDone() = {
    setRetrievalProcess("done")
    Cache.remove(serviceId)
    Cache.remove(progressId)
  }
  def setRetrievalProcessPending() = setRetrievalProcess("pending")


  def getRetrievalProcessStatus = Cache.get(serviceId)

  def waitForRetrievalProcessToBeDone(channel:Concurrent.Channel[String], pollingTimeout:Int):Unit = {
    getRetrievalProcessStatus match {
      case Some(status) =>
        channel push status.toString
        getRetrievalProcessProgress match {
          case Some(progress) => channel push("progress:" + progress.toString)
          case None => channel push "progress:1"
        }
        if(status != "done") {
          Thread.sleep(pollingTimeout)
          waitForRetrievalProcessToBeDone(channel, pollingTimeout)
        }
      case None => channel push "done"
    }
  }

  def setRetrievalProcessProgress(progress:Double) = Cache.set(progressId, progress, Duration(1, HOURS))

  def getRetrievalProcessProgress = Cache.get(progressId)

}
