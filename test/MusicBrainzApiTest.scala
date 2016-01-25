import models.service.api.discover.MusicBrainzApi
import models.util.Constants
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.test.WithApplication

class MusicBrainzApiTest extends UnitSpec {

  implicit val defaultPatience = PatienceConfig(timeout = Span(8, Seconds), interval = Span(500, Millis))

  val expectedResult = List(Map(Constants.mapKeyAlbum -> "The Sweetest Apples", Constants.mapKeyArtist -> "The Beatles"))

  "The API" should "find the correct album for a given artist and title" in new WithApplication {
    val apiRequest = MusicBrainzApi.findAlbumOfTrack("jealous guy", "beatles")
    whenReady(apiRequest) { res =>
      res should equal(expectedResult)
    }
  }

  "The API" should "retrieve more results for a low score" in new WithApplication {
    val apiRequest = MusicBrainzApi.findAlbumOfTrack("jealous guy", "beatles", 50)
    whenReady(apiRequest) { res =>
      res.toSet should equal(
        Set(Map(Constants.mapKeyAlbum -> "Unsurpassed Demos", Constants.mapKeyArtist -> "The Beatles")) ++ expectedResult
      )
    }
  }

  "The API" should "return None for an unknown artist" in new WithApplication {
    val apiRequest = MusicBrainzApi.isKnownArtist("The Undercover Heros")
    whenReady(apiRequest) { res =>
      res should be(None)
    }
  }

  "The API" should "return a name for an artist" in new WithApplication {
    val apiRequest = MusicBrainzApi.isKnownArtist("Beatles")
    whenReady(apiRequest) { res =>
      res should be(Some("The Beatles"))
    }
  }
}
