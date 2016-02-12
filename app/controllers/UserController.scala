package controllers

import models.User
import models.auth.AdminAccess
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Controller

class UserController extends Controller {

  def list = AdminAccess.async { implicit request =>
    User.list.map(res => Ok(views.html.users.list(res.toList)))
  }
}
