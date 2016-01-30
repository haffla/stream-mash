package controllers

import models.auth.MessageDigest
import models.User
import models.util.Constants
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import play.cache.Cache

import scala.concurrent.Future

class AuthController extends Controller with models.auth.form.Forms {

  def register = Action.async { implicit request =>

    registerForm.bindFromRequest.fold(
      formWithErrors => {
        val errors = formWithErrors.errors.map(error => error.messages)
        Future.successful(Ok(views.html.auth.register(errors.flatten.toList)))
      },
      user => {
        User.exists(user.name) flatMap { bool =>
          if(bool) {
            Future.successful(Redirect(routes.AuthController.register())
              .flashing("message" -> "Sorry. This username already exists."))
          }
          else {
            User.create(user.name, user.password) map { incrementId =>
              request.session.get(Constants.userSessionKey) match {
                case Some(sessionKey) =>
                  User.transferData(incrementId, sessionKey)
                case None => //NADA
              }
              val hash = authenticateUser(user.name, user.password)
              Redirect(routes.Application.index()).flashing("message" -> "Welcome")
                .withSession(Constants.username -> user.name, Constants.authSecret -> hash, Constants.userId -> incrementId.toString)
            }
          }
        }
      }
    )
  }

  def login = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(Redirect(routes.AuthController.login()))
      },
      userData => {
        User.getAccountByUserName(userData.name).map {
          case Some(user) =>
            if (user.password == MessageDigest.digest(userData.password)) {
              val username = user.name
              val password = user.password
              val incrementId = user.id
              val hash = authenticateUser(username, password)
              Some((true, hash, incrementId)) // Tuple[Boolean, String, Long]
            } else None
          case None => None
        } map {
          case Some(result) =>
            val newSession = request.session +
               (Constants.username -> userData.name) +
               (Constants.authSecret -> result._2)  +
               (Constants.userId -> result._3.toString)
            Redirect(request.session.get(Constants.intendedLocation).getOrElse("/")).withSession(newSession)
          case None =>
            Redirect(routes.AuthController.login()).flashing("message" -> "Username or password wrong")
        }
      }
    )
  }

  def logout = Action { implicit request =>
    val username = request.session.get(Constants.username)
    Cache.remove(s"user.$username")
    Redirect(routes.AuthController.login())
      .withNewSession
      .flashing("message" -> "You are logged out.")
  }

  //## Static

  def loginPage = Action { implicit request =>
    Ok(views.html.auth.login())
  }

  def registerPage = Action { implicit request =>
    Ok(views.html.auth.register(List.empty))
  }

  //## HELPER

  private def authenticateUser(username:String, password:String):String = {
    val hash = MessageDigest.digest(s"$username|$password")
    Cache.set(s"user.$username", hash)
    hash
  }
}
