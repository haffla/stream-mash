import models.database.alias.Artist
import models.service.api.{DeezerApiFacade, SpotifyApiFacade}
import org.scalatest.concurrent.ScalaFutures
import org.squeryl.PrimitiveTypeMode._
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.test.WithApplication

class ServiceApiTest extends UnitSpec {

  implicit val defaultPatience = PatienceConfig(timeout = Span(8, Seconds), interval = Span(500, Millis))

  val artist = Artist("Nicolas Jaar")
  val failureMsg = "Did not get an ID for artist"

  "The Spotify API" should "get a correct result for an artist search" in new WithApplication {
    val apiRequest = SpotifyApiFacade.getArtistId(artist)
    whenReady(apiRequest) {
      case Some(art) => art._2 should equal("5a0etAzO5V26gvlbmHzT9W")
      case _ => fail(failureMsg + " from Spotify")
    }
  }

  "The Spotify API" should "should return None for a senseless search" in new WithApplication {
      val apiRequest = SpotifyApiFacade.getArtistId(artist = Artist("Abracadabra Hero"), token = None, identifier = Some(Left(1)))
      whenReady(apiRequest) {
        res => res should be(None)
      }
  }

  "The Deezer API" should "get a correct result for an artist search" in new WithApplication {
    val apiRequest = DeezerApiFacade.getArtistId(artist)
    whenReady(apiRequest) {
      case Some(art) => art._2 should equal("294359")
      case _ => fail(failureMsg + " from Deezer")
    }
  }
}
