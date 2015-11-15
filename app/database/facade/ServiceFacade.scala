package database.facade

import database.MainDatabaseAccess
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import scalikejdbc.AutoSession
import slick.driver.JdbcProfile

class ServiceFacade extends MainDatabaseAccess with HasDatabaseConfig[JdbcProfile] {
  implicit val session = AutoSession
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
}
