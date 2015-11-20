package models.util

import play.api.mvc.Cookie

object TextWrangler {

  val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

  def generateRandomString(length:Int):String = {
    def buildString(s:String):String = {
      if(s.length >= length) s
      else {
        val rand = Math.random * (possible.length - 1)
        buildString(s + possible.charAt(rand.toInt))
      }
    }
    buildString("")
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
