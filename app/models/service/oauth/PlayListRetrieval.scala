package models.service.oauth

import models.util.Constants
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

trait PlayListRetrieval {

  protected def playlistRequest(accessToken:String):Future[WSResponse]
  protected def trackListLinks(js:JsValue):List[String]
  protected def getNextPage(trackJs: JsValue):(Boolean,String)

  private def retrievePlaylists(accessToken: String):Future[List[List[JsValue]]] = {
    playlistRequest(accessToken) flatMap { resp =>
      val js = Json.parse(resp.body)
      val links = trackListLinks(js)
      Future.sequence {
        links.map { trackListLink =>
          retrieveTracks(accessToken, trackListLink, Nil)
        }
      }
    }
  }

  private def retrieveTracks(accessToken: String, link:String, responses: List[JsValue]): Future[List[JsValue]] = {
    WS.url(link).get() flatMap { trackResponse =>
      val trackJs = Json.parse(trackResponse.body)
      val (tobeContinued, next) = getNextPage(trackJs)
      if(tobeContinued) retrieveTracks(accessToken, next, trackJs :: responses)
      else Future.successful(responses)
    }
  }

  def requestPlaylists(token:Option[String]):Future[Seq[JsValue]] = {
    token match {
      case Some(accessToken) =>
        retrievePlaylists(accessToken).map(x => x.flatten)
      case None => throw new Exception (Constants.accessTokenRetrievalError)
    }
  }
}
