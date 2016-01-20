package models.service.analysis

import models.database.alias.Artist
import models.database.facade.service.{ServiceAlbumFacade, ServiceArtistTrait}
import models.service.api.ApiFacade
import models.service.util.ServiceAccessTokenHelper
import models.util.Logging
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WS, WSRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class ServiceAnalysis(identifier:Either[Int,String],
                               usersFavouriteArtists: List[Artist],
                               service:String) {

  lazy val ich = this.getClass.toString
  val serviceAccessTokenHelper = new ServiceAccessTokenHelper(service, identifier)
  val searchEndpoint:String
  val serviceArtistFacade:ServiceArtistTrait
  val serviceAlbumFacade:ServiceAlbumFacade
  val apiFacade:ApiFacade

  def testAndGetAccessToken():Future[Option[String]]

  /**
    * returns a Tuple with the album name and the service id of the album
    */
  def handleJsonResponse(jsResp:JsValue):List[(String,String)]
  def getAuthenticatedRequest(url:String, accessToken:String):WSRequest
  def urlForRequest(artistId:String):String
  def getServiceFieldFromArtist(artist:Artist):Option[String]

  def getNextUrl(js:JsValue):Option[String] = {
    (js \ "next").asOpt[String]
  }

  def filterCachedArtists(usersFavouriteArtists: List[Artist]) = {
    val cachedArtist = serviceArtistFacade.allArtistIds
    usersFavouriteArtists.filter(a => !cachedArtist.contains(a.id))
  }

  def analyse():Future[Map[String, List[(String, String, String)]]] = {
    val artists = filterCachedArtists(usersFavouriteArtists)
    for {
      accessToken <- testAndGetAccessToken()
      ids <- artistIds(artists, accessToken)
      albums <- getArtistAlbumsFromService(ids, accessToken)
    } yield processResponses(albums)
  }

  private def artistIds(
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

  def artistsAlbumsRequest(url:String, token:Option[String], jsonResponses:List[JsValue]):Future[List[JsValue]] = {
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
          case Some(nextUrl) => artistsAlbumsRequest(nextUrl, token, js::jsonResponses)
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
        artistsAlbumsRequest(url,accessToken,Nil) map(jsonResponses => (artistName, jsonResponses))
      }
    }
  }

  private def processResponses(jsSet: List[(String,List[JsValue])]):Map[String,List[(String,String,String)]] = {
    val res = jsSet.foldLeft(Map[String,List[(String,String,String)]]()) { (prev, jsTuple) =>
      prev ++ processSingleResponse(jsTuple)
    }
    res
  }

  /**
    * Returns a map where the key is the artist name and the value a List of albums (name + id)
    */
  private def processSingleResponse(artistJsTuple:(String,List[JsValue])):Map[String,List[(String,String,String)]] = {
    val artist = artistJsTuple._1
    val jsResponseList = artistJsTuple._2
    val albums:List[(String,String,String)] = jsResponseList.flatMap { jsResp =>
      handleJsonResponse(jsResp).map { tuple =>
        val (albumName,albumServiceId) = tuple
        (albumName,albumServiceId,service)
      }
    }
    Map(artist -> albums)
  }
}
