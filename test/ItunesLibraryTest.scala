import models.service.library.ItunesLibrary
import play.Play
import play.api.test.WithApplication

import scala.concurrent.ExecutionContext.Implicits.global

class ItunesLibraryTest extends UnitSpec {

  "The ItunesLibrary" should "parse a library xml file correctly" in new WithApplication {
    val lib = new ItunesLibrary(Left(1), Play.application.path.getPath + "/test/resources/testItunesLibrary.xml", false)
    lib.saveCollection map { artistMap =>
      artistMap.size shouldEqual 2
      artistMap("Burial").size shouldEqual 2
      artistMap("Burial + Four Tet").size shouldEqual 2
    }
  }
}
