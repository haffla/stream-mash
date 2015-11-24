import models.service.library.ItunesLibrary
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.Play
import play.api.test.WithApplication

@RunWith(classOf[JUnitRunner])
class ItunesLibraryTest extends Specification {

  "The ItunesLibrary" should {
    "parse a library xml file correctly" in new WithApplication {
      val lib = new ItunesLibrary(Left(1), Play.application.path.getPath + "/test/resources/testItunesLibrary.xml")
      val artistMap = lib.getCollection
      artistMap.size shouldEqual 2
      artistMap("Burial").size shouldEqual 2
      artistMap("Burial + Four Tet").size shouldEqual 2
    }
  }
}
