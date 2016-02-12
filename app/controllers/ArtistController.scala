package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.database.facade.{ArtistFacade, ArtistLikingFacade}
import models.service.api.discover.EchoNestApi
import play.api.libs.json.Json
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ArtistController extends Controller {

  def rating = IdentifiedBySession { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    request.body.asJson.map { js =>
      val artist = (js \ "name").as[String]
      val score = (js \ "rating").as[Double]
      ArtistLikingFacade(identifier).setScoreForArtist(artist, score)
      Ok(Json.toJson(Map("success" -> true)))
    }.getOrElse(BadRequest("No request parameters found"))
  }

  def artistPic = IdentifiedBySession.async { implicit request =>
    request.getQueryString("artist").map { art =>
      ArtistFacade.artistPic(art).map { img =>
        Future.successful(Ok(Json.toJson(Map("img" -> img))))
      }.getOrElse {
        EchoNestApi.getArtistImage(art) map {
          case Some(img) => Ok(Json.toJson(Map("img" -> img)))
          case _ => Ok(Json.toJson(Map("error" -> "No picture found for artist")))
        }
      }
    }.getOrElse(Future.successful(BadRequest("No parameter 'artist' found")))
  }
}
