package models.service.util

import models.User
import play.api.cache.Cache
import play.api.Play.current

class ServiceAccessTokenCache(service:String, identifier:Either[Int,String]) {

  val id = identifier match {
    case Left(i) => i
    case Right(key) => key
  }
  val cacheKey = "access_token" + id
  
  def setAccessToken(token:Option[String]) = {
    token match {
      case Some(t) =>
        Cache.set(cacheKey + service, t)
        if(identifier.isLeft) User(identifier).setServiceToken(service, t)
      case None =>
    }
  }

  def getAccessToken:Option[String] = {
    val fromCache = Cache.getAs[String](cacheKey + service)
    fromCache match {
      case Some(token) => fromCache
      case None => User(identifier).getServiceToken(service)
    }
  }

}
