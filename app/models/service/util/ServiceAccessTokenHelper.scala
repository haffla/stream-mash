package models.service.util

import models.User
import play.api.cache.Cache
import play.api.Play.current

class ServiceAccessTokenHelper(service:String, identifier:Either[Int,String]) {

  val id = identifier match {
    case Left(i) => i
    case Right(key) => key
  }
  val cacheKey = "access_token" + id
  val refreshCacheKey = "refresh_token" + id
  
  def setAccessToken(token:String, refreshToken:Option[String] = None) = {
    Cache.set(cacheKey + service, token)
    refreshToken match {
      case Some(refreshTkn) => Cache.set(refreshCacheKey + service, refreshTkn)
      case _ =>
    }
    if(identifier.isLeft) User(identifier).setServiceToken(service, token, refreshToken)
  }

  def getAccessToken:Option[String] = {
    val fromCache = Cache.getAs[String](cacheKey + service)
    fromCache match {
      case Some(token) => fromCache
      case None => User(identifier).getServiceToken(service)
    }
  }

  def getRefreshToken:Option[String] = {
    val fromCache = Cache.getAs[String](refreshCacheKey + service)
    fromCache match {
      case Some(refreshTkn) => fromCache
      case None => User(identifier).getServiceRefreshToken(service)
    }
  }

  def getAnyAccessTokenPair:Option[models.Tokens] = {
    User.getAnyAccessTokenPair(service)
  }

  def getAnyAccessToken:Option[String] = {
    println("trying to get any access token")
    val x = User.getAnyAccessToken(service)
    println(x)
    x
  }

}
