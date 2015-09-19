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

import scala.concurrent.Await
import scala.concurrent.duration._

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

  def register = Action { implicit request =>
    val user = userForm.bindFromRequest.get
    val account = accountQuery.filter(_.name === user.name)
    var exists = true
    Await.result(
      db.run(account.exists.result).map( bool =>
        if(!bool) exists = false
      ), Duration.Inf)
    if(!exists) {
      User.create(user.name, user.password)
      val hash = authenticateUser(user.name, user.password)
      Redirect(routes.Application.index).flashing("message" -> "Welcome")
        .withSession("username" -> user.name, "auth-secret" -> hash)
    } else {
      Redirect(routes.AuthController.register)
        .flashing("message" -> "User already exists")
    }

  }

  def login = Action { implicit request =>
    var isAuthentic = false
    var hash = ""
    val user:UserData = userForm.bindFromRequest.get
    val existingUser = getAccountByUser(user).map(
      usr => usr.map(
        u => if(u.password == user.password) {
          isAuthentic = true
          val username = u.name
          val password = u.password
          hash = RosettaSHA256.digest(s"$username|$password")
          Cache.set(s"user.$username", hash)
        }
      )
    )
    Await.result(existingUser, Duration.Inf)
    println("asdasd")
    if(isAuthentic)
      Redirect(routes.Application.index).withSession("username" -> user.name, "auth-secret" -> hash)
    else
      Redirect(routes.AuthController.login).flashing("message" -> "Username or password wrong")
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

  def authenticateUser(username:String, password:String):String = {
    val hash = RosettaSHA256.digest(s"$username|$password")
    Cache.set(s"user.$username", hash)
    hash
  }


  //## HELPER

  def getAccountByUser(user:UserData) = {
    val account = accountQuery.filter(_.name === user.name)
    db.run(account.result)
  }
}
