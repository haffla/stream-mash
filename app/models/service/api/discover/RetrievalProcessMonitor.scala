package models.service.api.discover

import models.auth.Helper
import models.util.Logging
import play.api.cache.Cache
import play.api.Play.current
import play.api.libs.iteratee.Concurrent
import scala.concurrent.duration._
import scala.util.{Failure, Try}

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
        pushToChannel(channel, status.toString)
        getRetrievalProcessProgress match {
          case Some(progress) => pushToChannel(channel, "progress:" + progress.toString)
          case None => pushToChannel(channel, "progress:1")
        }
        if(status != "done") {
          Thread.sleep(pollingTimeout)
          waitForRetrievalProcessToBeDone(channel, pollingTimeout)
        }
      case None => pushToChannel(channel, "done")
    }
  }

  def setRetrievalProcessProgress(progress:Double) = Cache.set(progressId, progress, Duration(1, HOURS))

  def getRetrievalProcessProgress = Cache.get(progressId)

  def pushToChannel(channel:Concurrent.Channel[String], message:String) = {
    Try {
      channel push message
    } match {
      case Failure(_) => Logging.error(this.getClass.toString, "Was trying to push message: " + message)
      case _ =>
    }
  }

}
