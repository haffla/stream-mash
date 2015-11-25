import models.service.api.discover.MusicBrainzApi
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.test.WithApplication

class MusicBrainzApiTest extends UnitSpec with ScalaFutures {

  implicit val defaultPatience = PatienceConfig(timeout = Span(2, Seconds), interval = Span(500, Millis))

  val expectedResult = List(Map("album" -> "The Sweetest Apples", "artist" -> "The Beatles"))

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
        Set(Map("album" -> "Unsurpassed Demos", "artist" -> "The Beatles")) ++ expectedResult
      )
    }
  }
}
