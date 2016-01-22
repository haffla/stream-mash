import models.database.facade.ArtistFacade
import play.api.test.WithApplication

class ArtistFacadeTest extends UnitSpec {
  "The Artist Facade" should "get favourite albums" in new WithApplication {
    val johnnyId = ArtistFacade.insert("Johnny Cash")
    val nicolasId = ArtistFacade.insert("Nicolas")
    val hansId = ArtistFacade.insert("Hans")

  }
}
