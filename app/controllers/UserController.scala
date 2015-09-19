package controllers

import models.{UserData, User}
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class UserController extends Controller {

  def list = Authenticated.async {
    val users:Future[Seq[User.Account#TableElementType]] = User.list
    users.map(
      res => Ok(views.html.users.list(res.toList))
    )
  }
}
