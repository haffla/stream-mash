
import controllers.{DeezerController, SpotifyController}
import models.util.Constants
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.libs.json.JsValue
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ServiceAnalysisControllerTest extends PlaySpec with OneServerPerSuite with Results {

  class SpoCtrl extends SpotifyController
  class DeeCtrl extends DeezerController

  "A Service controller" must {
    "return valid JSON for a user with user_session" in {
      val con = new SpoCtrl
      val result = con.getArtistsForOverview().apply(FakeRequest().withSession(Constants.userSessionKey -> "asdafhnsuiohsfnjk3"))
      status(result) must be(OK)
      val bodyText: JsValue = contentAsJson(result)
      (bodyText \ "data" \ Constants.jsonKeyArtists).as[List[JsValue]].length must be(0)
      (bodyText \ "data" \ Constants.jsonKeyStats \ Constants.jsonKeyAlbumsOnlyInUserCollection).as[List[JsValue]].length must be(0)
    }
  }

  "A Service controller" must {
    "deny a user who is trying to fake a request" in {
      val con = new DeeCtrl
      val result = con.getArtistsForOverview().apply(FakeRequest().withSession(Constants.userId -> "1"))
      status(result) must be(SEE_OTHER)
      contentAsString(result) must be("")
    }
  }
}
