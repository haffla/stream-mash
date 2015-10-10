import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/doesnotexist")) must beSome.which (status(_) == NOT_FOUND)
    }

    "redirect to login page" in new WithApplication{
      val Some(home) = route(FakeRequest(GET, "/"))

      status(home) must equalTo(SEE_OTHER)
      redirectLocation(home) must beSome.which(_ == "/login")
    }
  }
}
