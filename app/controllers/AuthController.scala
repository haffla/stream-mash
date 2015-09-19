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

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.util.Success

class AuthController extends Controller
        with AccountTable
        with HasDatabaseConfig[JdbcProfile]{

  import driver.api._
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  val accountQuery = TableQuery[Account]
  val userForm = Form(
    mapping(
      "name" -> text,
      "password" -> text
    )(UserData.apply)(UserData.unapply)
  )

  def register = Action.async { implicit request =>
    val user = userForm.bindFromRequest.get
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
        if(result.nonEmpty && result.head._1 == true) {
          println(result)
          Redirect(routes.Application.index).withSession("username" -> userData.name, "auth-secret" -> result.head._2)
        }

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
    Ok(views.html.auth.register())
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
