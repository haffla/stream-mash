package models.auth

import java.util.Base64

object MessageDigest {
  def digest(s: String, method:String = "SHA-256"): String = {
    val m = java.security.MessageDigest.getInstance(method).digest(s.getBytes("UTF-8"))
    m.map("%02x".format(_)).mkString
  }

  def md5(s:String): String = {
    digest(s, "MD5")
  }

  def encodeBase64(s:String): String = {
    Base64.getEncoder.encodeToString(s.getBytes())
  }
}