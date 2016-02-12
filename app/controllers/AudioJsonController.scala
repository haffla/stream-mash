package controllers

import models.auth.{IdentifiedBySession, Helper}
import models.service.importer.AudioJsonImporter
import play.api.libs.json.Json
import play.api.mvc.Controller

class AudioJsonController extends Controller {

  def handleAudioJson = IdentifiedBySession { implicit request =>
    val identifier = Helper.getUserIdentifier(request.session)
    request.body.asJson.map { js =>
      AudioJsonImporter(identifier).process(js)
      Ok(Json.obj("redirect" -> routes.CollectionController.index("audio").toString))
    }.getOrElse(
      Ok(Json.obj("error" -> true, "reason" -> "No Json found!"))
    )
  }
}
