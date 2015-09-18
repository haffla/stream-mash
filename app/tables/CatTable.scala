package tables

import java.sql.Timestamp

import models.Cat
import slick.driver.JdbcProfile

trait CatTable {
  protected val driver: JdbcProfile
  import driver.api._
  class Cats(tag: Tag) extends Table[Cat](tag, "cat") {

    def name = column[String]("name", O.PrimaryKey)
    def color = column[String]("color")
    def age = column[Int]("age")
    def created_at = column[Timestamp]("created_at")

    def * = (name, color, age) <> ((Cat.apply _).tupled, Cat.unapply _)
  }
}