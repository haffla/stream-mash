package models.service.analysis

import models.service.util.ServiceAccessTokenHelper

abstract class ServiceAnalysis(identifier:Either[Int,String], service:String) {
  val serviceAccessTokenHelper = new ServiceAccessTokenHelper(service, identifier)
}
