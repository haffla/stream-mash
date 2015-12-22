import models.database.alias.{Bar, AppDB}

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.squeryl.PrimitiveTypeMode.inTransaction

import play.api.test.WithApplication

class BarSpec extends FlatSpec with Matchers {

  "A Bar" should "be creatable" in new WithApplication {
      inTransaction {
        val bar = AppDB.barTable insert Bar(Some("foo"))
        bar.id should not equal 0
      }
  }

}