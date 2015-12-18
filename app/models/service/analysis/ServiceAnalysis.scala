package models.service.analysis

import models.service.util.ServiceAccessTokenCache

abstract class ServiceAnalysis(identifier:Either[Int,String], service:String) {
  val serviceAccessTokenCache = new ServiceAccessTokenCache(service, identifier)
}
