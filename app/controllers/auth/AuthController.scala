package controllers.auth

import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import play.api.mvc._
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import tables.AccountTable
import play.api.data._
import play.api.data.Forms._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class AuthController extends Controller
        with AccountTable
        with HasDatabaseConfig[JdbcProfile]{

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._

  case class UserData(name: String, password: String)

  val accountQuery = TableQuery[Account]

  val userForm = Form(
    mapping(
      "name" -> text,
      "password" -> text
    )(UserData.apply)(UserData.unapply)
  )

  def logout = Action {implicit request =>
    Ok("Bye").withNewSession
  }

  def affe = Authenticated {
    Ok("Affe!")
  }

  def login = Action { implicit request =>
    val user = userForm.bindFromRequest.get
    val account = accountQuery.filter(_.name === user.name)
    var isAuthentic:Boolean = false
    val existingUser = db.run(account.result).map(
      usr => usr.map(
        u => if(u.password == user.password)
                isAuthentic = true
      )
    )
    Await.result(existingUser, 3.second)
    if(isAuthentic)
      Ok("Alles klar").withSession("username" -> user.name)
    else
      Ok(views.html.auth.login()).flashing("message" -> "Username or password wrong")
  }

  def loginPage = Action { implicit request =>
    Ok(views.html.auth.login())
  }
}
