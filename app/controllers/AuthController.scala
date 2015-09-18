package controllers

import models.auth.RosettaSHA256
import models.UserData
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

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._

  val accountQuery = TableQuery[Account]

  val userForm = Form(
    mapping(
      "name" -> text,
      "password" -> text
    )(UserData.apply)(UserData.unapply)
  )

  def logout = Action { implicit request =>
    val username = request.session.get("username")
    Cache.remove(s"user.$username")
    Ok("Bye").withNewSession
  }

  def login = Action { implicit request =>
    val user = userForm.bindFromRequest.get
    val account = accountQuery.filter(_.name === user.name)
    var isAuthentic:Boolean = false
    var hash = ""
    val existingUser = db.run(account.result).map(
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
    if(isAuthentic)
      Redirect(routes.Application.index).withSession("username" -> user.name, "auth-secret" -> hash)
    else
      Redirect(routes.Application.index).flashing("message" -> "Username or password wrong")
  }

  def loginPage = Action { implicit request =>
    Ok(views.html.auth.login())
  }
}
