package models.service.analysis

import models.database.alias.Artist
import models.database.facade._
import models.database.facade.service.{ServiceAlbumFacade, ServiceArtistTrait}
import models.service.api.ApiFacade
import models.service.util.ServiceAccessTokenHelper
import models.util.Logging
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.{WS, WSRequest}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

abstract class ServiceAnalysis(identifier:Either[Int,String], service:String) {

  lazy val ich = this.getClass.toString
  val serviceAccessTokenHelper = new ServiceAccessTokenHelper(service, identifier)
  val searchEndpoint:String
  val albumFacade:AlbumFacade = new AlbumFacade(identifier)
  val artistFacade:ArtistFacade = new ArtistFacade(identifier)
  val serviceArtistFacade:ServiceArtistTrait
  val serviceAlbumFacade:ServiceAlbumFacade
  val apiFacade:ApiFacade

  def testAndGetAccessToken():Future[Option[String]]
  def handleJsonResponse(jsResp:JsValue):List[(String,String)]
  def getAuthenticatedRequest(url:String, accessToken:String):WSRequest
  def urlForRequest(artistId:String):String
  def getServiceFieldFromArtist(artist:Artist):Option[String]

  def getNextUrl(js:JsValue):Option[String] = {
    (js \ "next").asOpt[String]
  }

  def analyse():Future[Boolean] = {
    val artists = artistFacade.getUsersArtists
    for {
      accessToken <- testAndGetAccessToken()
      ids <- getIds(artists, accessToken)
      albums <- getArtistAlbumsFromService(ids, accessToken)
      res = processResponses(albums)
    } yield true
  }

  private def getIds(
              artists: List[models.database.alias.Artist],
              token:Option[String]):Future[List[Option[(String,String)]]] = {
    if(artists.nonEmpty) {
      Future.sequence {
        artists.map { artist =>
          getServiceFieldFromArtist(artist) match {
            case Some(spoId) => Future.successful(Some(artist.name, spoId))
            case None => apiFacade.getArtistId(artist.name, token, Some(identifier))
          }
        }
      }
    }
    else Future.successful(Nil)
  }

  def doRequest(url:String, token:Option[String], jsonResponses:List[JsValue]):Future[List[JsValue]] = {
    val request = token match {
      case Some(tkn) => getAuthenticatedRequest(url, tkn)
      case _ => WS.url(url)
    }
    request.get().flatMap { response =>
      if(response.status != 200) {
        Logging.debug(ich, response.body.toString + "\n" + response.status + "\n")
        Future.successful(jsonResponses)
      }
      else {
        val js = Json.parse(response.body)
        getNextUrl(js) match {
          case Some(nextUrl) => doRequest(nextUrl, token, js::jsonResponses)
          case _ => Future.successful(js::jsonResponses)
        }
      }
    }
  }

  private def getArtistAlbumsFromService(
                                  ids: List[Option[(String,String)]],
                                  accessToken:Option[String]):Future[List[(String,List[JsValue])]] = {
    val searchList:List[(String,String)] = ids.filter(_.isDefined).map(_.get)
    Future.sequence {
      searchList map { artist =>
        val artistName = artist._1
        val artistId = artist._2
        val url = urlForRequest(artistId)
        doRequest(url,accessToken,Nil) map(jsonResponses => (artistName, jsonResponses))
      }
    }
  }

  private def save(artistAlbumMap: Map[String,List[(String,String)]]) = {
    artistAlbumMap.foreach { entity =>
      val artist = entity._1
      val albums = entity._2
      val dbArtistId:Long = serviceArtistFacade.saveArtistWithName(artist)
      albums.foreach { alb =>
        val albumName = alb._1
        val id = alb._2
        serviceAlbumFacade.saveAlbumWithNameAndId(albumName, dbArtistId, id)
      }
    }
  }

  private def processResponses(jsSet: List[(String,List[JsValue])]):Map[String,List[(String,String)]] = {
    val res = jsSet.foldLeft(Map[String,List[(String,String)]]()) { (prev, jsTuple) =>
      prev ++ processSingleResponse(jsTuple)
    }
    save(res)
    res
  }

  private def processSingleResponse(artistJsTuple:(String,List[JsValue])):Map[String,List[(String,String)]] = {
    val artist = artistJsTuple._1
    val jsResponseList = artistJsTuple._2
    val albums:List[(String,String)] = jsResponseList.flatMap { jsResp =>
      handleJsonResponse(jsResp)
    }
    Map(artist -> albums)
  }
}
