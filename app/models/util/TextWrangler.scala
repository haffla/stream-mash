package models.util

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
}
