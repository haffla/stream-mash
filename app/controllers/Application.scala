package controllers

import models.Cat
import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import slick.driver.JdbcProfile
import tables.CatTable

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class Application extends Controller
      with CatTable
      with HasDatabaseConfig[JdbcProfile]{
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._

  //create an instance of the table
  val catsQuery = TableQuery[Cats]

  val catForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "color" -> nonEmptyText,
      "age" -> number
    )(Cat.apply)(Cat.unapply)
  )

  def index = Authenticated.async { implicit request =>
    db.run(catsQuery.sortBy(_.created_at.desc).result).map(
      res => Ok(views.html.index(res.toList)))
  }

  def insert = Authenticated.async { implicit request =>
    catForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(Redirect(routes.Application.index).flashing("message" -> "The form must be complete"))
        },
        cat => {
          var message = ""
          val catWithThatName = catsQuery.filter(_.name === cat.name)
          val catWasSaved = db.run(catWithThatName.exists.result).map(existsAlready =>
            if(!existsAlready) {
              message = "Cat created"
              db.run(catsQuery += cat)
            } else {
              message = "This cat already exists"
            }
          )
          // need to block here and wait for result, so user gets flash message.
          Await.result(catWasSaved, 7.second)
          Future.successful(Redirect(routes.Application.index).flashing("message" -> message))
        }
      )
  }
}