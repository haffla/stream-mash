package models.service.oauth

import models.service.Constants
import models.util.Logging
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.WSResponse

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait FavouriteMusicRetrieval {

  def favouriteMusicRetrievalRequest(accessToken:String):Future[WSResponse]

  def requestUsersTracks(token:Option[String]):Future[Option[JsValue]] = {
    token match {
      case Some(accessToken) =>
        favouriteMusicRetrievalRequest(accessToken) map { response =>
          response.status match {
            case 200 =>
              val json = Json.parse(response.body)
              Some(json)
            case http_code =>
              Logging.error(this.getClass.toString, Constants.userTracksRetrievalError + ": " +  http_code + "\n" + response.body)
              None
          }
        }
      case None => throw new Exception (Constants.accessTokenRetrievalError)
    }
  }
}
