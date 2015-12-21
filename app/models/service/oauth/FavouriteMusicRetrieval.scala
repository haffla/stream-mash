package models.service.oauth

import models.service.Constants
import models.util.Logging
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.WSResponse

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait FavouriteMusicRetrieval {

  def favouriteMusicRetrievalRequest(accessToken:String, page:String):Future[WSResponse]

  def getPageInformation(json:JsValue):(Boolean,Int)

  def requestUsersTracks(token:Option[String]):Future[Seq[JsValue]] = {
    token match {
      case Some(accessToken) =>
        doIt(accessToken, Seq.empty, 1)
      case None => throw new Exception (Constants.accessTokenRetrievalError)
    }
  }

  def doIt(accessToken:String, aggregatedResponses:Seq[JsValue], pageCount:Int):Future[Seq[JsValue]] = {
    favouriteMusicRetrievalRequest(accessToken, pageCount.toString) flatMap  { response =>
      response.status match {
        case 200 =>
          val json = Json.parse(response.body)
          val (tobeContinued, next) = getPageInformation(json)
          if(tobeContinued) doIt(accessToken, aggregatedResponses :+ json, next)
          else Future.successful(aggregatedResponses :+ json)
        case http_code =>
          Logging.error(this.getClass.toString, Constants.userTracksRetrievalError + ": " +  http_code + "\n" + response.body)
          Future.successful(aggregatedResponses)
      }
    }
  }
}
