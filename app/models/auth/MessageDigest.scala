package models.auth

object MessageDigest {
  def digest(s: String, method:String = "SHA-256"): String = {
    val m = java.security.MessageDigest.getInstance(method).digest(s.getBytes("UTF-8"))
    m.map("%02x".format(_)).mkString
  }

  def md5(s:String): String = {
    digest(s, "MD5")
  }

  def encodeBase64(s:String): String = {
    new sun.misc.BASE64Encoder().encode(s.getBytes)
  }
}