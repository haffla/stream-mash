package models.util

import play.api.Logger

object Logging {
  val logger = Logger.logger
  def debug(context:String, message:String) = {
    logger.debug(s"$context: $message")
  }
  def error(context:String, message:String) = {
    logger.error(s"$context: $message")
  }

  def info(context:String, message:String) = {
    logger.info(s"$context: $message")
  }
}
