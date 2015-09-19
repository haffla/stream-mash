package controllers

import models.auth.RosettaSHA256
import models.{User, UserData}
import play.api.Play
import play.api.data.Forms._
import play.api.data._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.cache.Cache
import slick.driver.JdbcProfile
import tables.AccountTable

import scala.concurrent.Future

class AuthController extends Controller
        with AccountTable
        with HasDatabaseConfig[JdbcProfile]{

  import driver.api._
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  val accountQuery = TableQuery[Account]
  val USERNAME_MAX_LENGTH = 64
  val USERNAME_MIN_LENGTH = 3
  val PASSWORD_MIN_LENGTH = 6

  val userForm = Form(
    mapping(
      "name" -> text.verifying(s"The username's length must be between $USERNAME_MIN_LENGTH and $USERNAME_MAX_LENGTH",
                                    text => text.length > USERNAME_MIN_LENGTH && text.length < USERNAME_MAX_LENGTH),
      "password" -> tuple(
        "main" -> text.verifying(s"The password must be at least $PASSWORD_MIN_LENGTH characters long",
                                    password => password.length > PASSWORD_MIN_LENGTH),
        "confirm" -> text
      ).verifying(
          "Passwords don't match", password => password._1 == password._2
        )
        .transform(
      { case (main, confirm) => main },
      (main: String) => ("", "")
      )
    )(UserData.apply)(UserData.unapply)
  )

  def register = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        val errors = formWithErrors.errors.map(error => error.messages)
        Future.successful(Ok(views.html.auth.register(errors.flatten.toList)))
      },
      user => {
        val userExists = User.exists(user.name)
        userExists.map( bool =>
          if(!bool) {
            User.create(user.name, user.password)
            val hash = authenticateUser(user.name, user.password)
            Redirect(routes.Application.index).flashing("message" -> "Welcome")
              .withSession("username" -> user.name, "auth-secret" -> hash)
          } else {
            Redirect(routes.AuthController.register)
              .flashing("message" -> "User already exists")
          }
        )
      }
    )

  }

  def login = Action.async { implicit request =>
    val userData:UserData = userForm.bindFromRequest.get
    getAccountByUser(userData).map(
      listOfUsers => listOfUsers.map(
        user => if(user.password == userData.password) {
          val username = user.name
          val password = user.password
          val hash = authenticateUser(username, password)
          (true, hash) // Tuple[Boolean, String]
        } else (false, "")
      )
    ).map( result => // is a Seq[Tuple[Boolean,String]]
        if(result.nonEmpty && result.head._1)
          Redirect(routes.Application.index).withSession("username" -> userData.name, "auth-secret" -> result.head._2)
        else
          Redirect(routes.AuthController.login).flashing("message" -> "Username or password wrong")
      )

  }

  def logout = Action { implicit request =>
    val username = request.session.get("username")
    Cache.remove(s"user.$username")
    Redirect(routes.AuthController.login).withNewSession
  }

  //## Static

  def loginPage = Action { implicit request =>
    Ok(views.html.auth.login())
  }

  def registerPage = Action { implicit request =>
    Ok(views.html.auth.register(List.empty))
  }


  //## HELPER

  def getAccountByUser(user:UserData) = {
    val account = accountQuery.filter(_.name === user.name).take(1)
    db.run(account.result)
  }

  def authenticateUser(username:String, password:String):String = {
    val hash = RosettaSHA256.digest(s"$username|$password")
    Cache.set(s"user.$username", hash)
    hash
  }
}
