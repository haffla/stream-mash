package models.service.oauth

import models.util.Constants
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WS, WSRequest, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait PlayListRetrieval {

  protected def playlistRequest(accessToken:String):Future[WSResponse]
  protected def trackListLinks(js:JsValue):List[String]
  protected def getNextPage(trackJs: JsValue):(Boolean,String)
  protected def authenticateTrackRetrievalRequest(wsRequest: WSRequest, accessToken:String):WSRequest

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

  protected def extractTracksFromJs(trackJs: JsValue) = trackJs

  private def retrieveTracks(accessToken: String, link:String, responses: List[JsValue]): Future[List[JsValue]] = {
    authenticateTrackRetrievalRequest(WS.url(link), accessToken).get() flatMap { trackResponse =>
      val trackJs = Json.parse(trackResponse.body)
      val (tobeContinued, next) = getNextPage(trackJs)
      val js = extractTracksFromJs(trackJs)
      if(tobeContinued) retrieveTracks(accessToken, next, js :: responses)
      else Future.successful(js :: responses)
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
