package models.auth

object RosettaSHA256 extends App {
  def digest(s: String, method:String = "SHA-256"): String = {
    val m = java.security.MessageDigest.getInstance(method).digest(s.getBytes("UTF-8"))
    m.map("%02x".format(_)).mkString
  }

  def md5(s:String): String = {
    digest(s, "MD5")
  }
}