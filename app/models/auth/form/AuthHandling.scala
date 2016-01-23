package models.auth.form

import play.api.mvc.{Controller, Request}

import scala.concurrent.Future


trait AuthHandling extends Controller {
  def redirectTo[A](page: String, request:Request[A], message:String) = {
    Future.successful(
      Redirect(page).flashing("message" -> message)
        .withSession(request.session + ("intended_location" -> request.path))
    )
  }
}
