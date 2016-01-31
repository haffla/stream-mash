package models.util

import play.api.libs.concurrent.Akka
import play.api.Play.current

import scala.concurrent.ExecutionContext

object ThreadPools {
  implicit val analysisExecutionContext: ExecutionContext = Akka.system.dispatchers.lookup("akka.actor.analysis")
}
