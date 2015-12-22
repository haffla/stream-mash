import play.api.mvc.RequestHeader
import play.api.mvc.Results._
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.internals.DatabaseAdapter
import org.squeryl.{Session, SessionFactory}

import play.api.Application
import scala.concurrent.Future


object Global extends play.api.GlobalSettings {

  override def onStart(app: Application) {
    SessionFactory.concreteFactory = app.configuration.getString("db.default.driver") match {
      case Some("org.postgresql.Driver") => Some(() => getSession(new PostgreSqlAdapter, app))
      case _ => sys.error("Database driver must be org.postgresql.Driver")
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

  override def onError(request: RequestHeader, ex: Throwable) = {
    val exceptionMessage = ex.getMessage
    Future.successful(InternalServerError(views.html.error(
      Map(
        "exceptionMessage" -> exceptionMessage
      )
    )))
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