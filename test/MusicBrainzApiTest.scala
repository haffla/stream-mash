import models.service.api.discover.MusicBrainzApi
import models.util.Constants
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.test.WithApplication

class MusicBrainzApiTest extends UnitSpec {

  implicit val defaultPatience = PatienceConfig(timeout = Span(8, Seconds), interval = Span(500, Millis))

  "The API" should "return None for an unknown artist" in new WithApplication {
    val apiRequest = MusicBrainzApi.isKnownArtist("The Undercover Heros")
    whenReady(apiRequest) {
      case Some("unavailable") =>
      case res => res should be(None)
    }
  }

  "The API" should "return a name for an artist" in new WithApplication {
    val apiRequest = MusicBrainzApi.isKnownArtist("Beatles")
    whenReady(apiRequest) {
      case Some("unavailable") =>
      case res => res should be(Some("The Beatles"))
    }
  }
}
