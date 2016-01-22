import org.squeryl.adapters.{H2Adapter, PostgreSqlAdapter}
import org.squeryl.internals.DatabaseAdapter
import org.squeryl.{Session, SessionFactory}
import play.api.Application
import play.api.mvc.RequestHeader
import play.api.mvc.Results._

import scala.concurrent.Future


object Global extends play.api.GlobalSettings {

  override def onStart(app: Application) {
    SessionFactory.concreteFactory = app.configuration.getString("db.default.driver") match {
      case Some("org.postgresql.Driver") =>
        Some(() => getSession(new PostgreSqlAdapter, app))
      case Some("org.h2.Driver") =>
        Some(() => getSession(new H2Adapter, app))
      case _ => sys.error("Database driver must be either org.h2.Driver or org.postgresql.Driver")
    }
  }

  def getSession(adapter:DatabaseAdapter, app: Application) = {
    (app.configuration.getString("db.default.password"),
      app.configuration.getString("db.default.username"),
      app.configuration.getString("db.default.url")) match {

      case (Some(password), Some(username), Some(url)) =>
        Session.create(
          java.sql.DriverManager.getConnection(url, username, password
          ), adapter)
      case _ => sys.error("Database user, url and password must be configured")
    }
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    val path = request.path
    Future.successful(NotFound(views.html.error(
      Map(
        "title" -> "Oups. The page you were looking for does not exist.",
        "path" -> path
      )
    )))
  }
}