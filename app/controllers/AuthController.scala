package controllers

import models.auth.MessageDigest
import models.service.Constants
import models.User
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
                  //TODO User.transferData(incrementId, sessionKey)
                case None => //NADA
              }
              val hash = authenticateUser(user.name, user.password)
              Redirect(routes.Application.index()).flashing("message" -> "Welcome")
                .withSession("username" -> user.name, "auth-secret" -> hash, "user_id" -> incrementId.toString)
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
              Some((true, hash, incrementId.get)) // Tuple[Boolean, String, Int]
            } else None
          case None => None
        } map {
          case Some(result) =>
            request.session.get(Constants.userSessionKey) match {
              case Some(sessionKey) =>
                //TODO User.transferData(result._3, sessionKey)
              case None => //NADA
            }
            val newSession = request.session +
               ("username" -> userData.name) +
               ("auth-secret" -> result._2)  +
               ("user_id" -> result._3.toString)
            Redirect(request.session.get("intended_location").getOrElse("/")).withSession(newSession)
          case None =>
            Redirect(routes.AuthController.login()).flashing("message" -> "Username or password wrong")
        }
      }
    )
  }

  def logout = Action { implicit request =>
    val username = request.session.get("username")
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
