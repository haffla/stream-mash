package controllers

import models.auth.MessageDigest
import models.database.MainDatabaseAccess
import models.{User, UserData}
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import play.cache.Cache
import slick.driver.JdbcProfile

import scala.concurrent.Future

class AuthController extends Controller
        with MainDatabaseAccess
        with HasDatabaseConfig[JdbcProfile]
        with models.auth.form.Forms {

  import driver.api._
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

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
        val errors = formWithErrors.errors
        Future.successful(Redirect(routes.AuthController.login()))
      },
      userData => {
        getAccountByUser(userData).map {
          case Some(user) =>
            if (user.password == MessageDigest.digest(userData.password)) {
              val username = user.name
              val password = user.password
              val incrementId = user.id
              val hash = authenticateUser(username, password)
              Some((true, hash, incrementId.get)) // Tuple[Boolean, String, Int]
            } else None
          case None => None
        }.map {
          case Some(result) =>
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

  private def getAccountByUser(user:UserData) = {
    val account = accountQuery.filter(_.name === user.name).take(1)
    db.run(account.result.headOption)
  }

  private def authenticateUser(username:String, password:String):String = {
    val hash = MessageDigest.digest(s"$username|$password")
    Cache.set(s"user.$username", hash)
    hash
  }
}
