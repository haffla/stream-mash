import java.sql.DriverManager

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.squeryl.Session
import org.squeryl.adapters.H2Adapter
import play.api.Play
import play.api.test.FakeApplication

abstract class UnitSpec extends FlatSpec with Matchers with
  OptionValues with Inside with Inspectors with ScalaFutures {

  lazy val app: FakeApplication = FakeApplication()

  def squerylSession = Play.current.configuration.getString("db.default.url") match {
    case Some(url) => Session.create(DriverManager.getConnection(url,"sa",""),new H2Adapter)
    case _ => throw new Exception("No database url defined! Cannot start test.")
  }
}