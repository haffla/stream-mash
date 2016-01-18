package controllers

import models.auth.{Helper, IdentifiedBySession}
import models.service.analysis.{NapsterAnalysis, DeezerAnalysis, SpotifyAnalysis}
import play.api.libs.json.Json
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global

class CollectionController extends Controller {

  def index(service:String = "") = IdentifiedBySession { implicit request =>
    Ok(views.html.collection.index(service))
  }

  def overview() = IdentifiedBySession { implicit request =>
    Ok(views.html.collection.overview())
  }

  def analysis = IdentifiedBySession.async { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    for {
      deezerResult <- DeezerAnalysis(identifier).analyse()
      spotifyResult <- SpotifyAnalysis(identifier).analyse()
      napsterResult <- NapsterAnalysis(identifier).analyse()
    } yield Ok(Json.obj(
                  "successDeezer" -> napsterResult,
                  "successSpotify" -> spotifyResult,
                  "successNapster" -> napsterResult))
  }
}
