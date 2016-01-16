package controllers

import models.auth.IdentifiedBySession
import play.api.mvc.Controller

class CollectionController extends Controller {

  def index(service:String = "", openModal:String = "no") = IdentifiedBySession { implicit request =>
    Ok(views.html.collection.index(service, openModal))
  }

  def overview() = IdentifiedBySession { implicit request =>
    Ok(views.html.collection.analysis())
  }
}
