package models.util

import play.api.mvc.Cookie

import scala.util.Random

object TextWrangler {

  def generateRandomString(length:Int):String = {
    Random.alphanumeric.take(length).mkString
  }

  def removeBrackets(s:String):String = {
    s.replaceAll("([\\(\\[].*[\\)\\]])", "")
  }

  def removeSpecialCharsAndWhiteSpace(s:String):String = {
    s.replaceAll("[^\\p{L}0-9_\\- ]", "").replaceAll(" +", " ").trim
  }

  def cleanupString(s:String):String = {
    removeSpecialCharsAndWhiteSpace(removeBrackets(s))
  }

  /**
   * Protects from CSRF attacks by comparing a certain value of a response
   * with a stored value saved in a Cookie during the request
   */
  def validateState(cookie: Option[Cookie], state:Option[String]):Boolean = {
      val storedState:Option[String] = cookie match {
        case Some(cookie) => Some(cookie.value)
        case None => None
      }
      storedState match {
        case Some(storedStateValue) =>
          state match {
            case Some(stateValue) =>
              storedStateValue == stateValue
            case None => false
          }
        case None => false
      }
  }
}
