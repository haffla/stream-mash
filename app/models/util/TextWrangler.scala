package models.util

object TextWrangler {

  val POSSIBLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

  def generateRandomString(length:Int):String = {
    def buildString(s:String):String = {
      if(s.length >= length) s
      else {
        val rand = Math.random * (POSSIBLE.length - 1)
        buildString(s + POSSIBLE.charAt(rand.toInt))
      }
    }
    buildString("")
  }
}
