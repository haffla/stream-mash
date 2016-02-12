import models.service.importer.ItunesImporter
import play.Play
import play.api.test.WithApplication

import scala.concurrent.ExecutionContext.Implicits.global

class ItunesImporterTest extends UnitSpec {

  "The ItunesLibrary" should "parse a library xml file correctly" in new WithApplication {
    val lib = new ItunesImporter(Left(1), false)
    lib.processAndSave(Play.application.path.getPath + "/test/resources/testItunesLibrary.xml") map { artistMap =>
      artistMap.size shouldEqual 2
      artistMap("Burial").size shouldEqual 2
      artistMap("Burial + Four Tet").size shouldEqual 2
    }
  }
}
