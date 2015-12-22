package models.database.facade

import scalikejdbc.AutoSession
trait Facade {
  implicit val session = AutoSession
}
