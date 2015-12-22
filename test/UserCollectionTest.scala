import models.User
import models.database.facade.CollectionFacade
import models.service.library._
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.JsValue
import play.api.test.WithApplication

class UserCollectionTest extends UnitSpec with ScalaFutures {

  val identifier = Right("testusersession")

  "Creating albums, saving them to DB and retrieving them" should "work" in new WithApplication {
    val collection = Map(
      "The Beatles" -> Map("Sgt. Pepper’s Lonely Hearts Club Band" -> Set("With a Little Help from My Friends", "Getting Better")),
      "Extrawelt" -> Map("Dark Side Of The Moon" -> Set("Track One", "Track Two")),
      "Apparat" -> Map("Walls" -> Set("Walls"))
    )

    val library = new Library(identifier)
    library.persist(collection)

    Thread.sleep(1000) // Need to wait a little for the data to be saved in DB

    val fromDb = CollectionFacade(identifier).userCollection
    whenReady(fromDb) { col =>
      val forFrontEnd = library.prepareCollectionForFrontend(col)

      val converted:List[JsValue] = forFrontEnd.as[List[JsValue]]
      val names = converted map(x => (x \ "name").as[String])

      val albums = converted map(x => (x \ "albums").as[List[JsValue]]) flatMap { albumSet =>
        albumSet map { album =>
          (album \ "name").as[String]
        }
      }

      names.toSet should equal(Set("The Beatles", "Apparat", "Extrawelt"))
      albums.toSet should equal(Set("Sgt. Pepper’s Lonely Hearts Club Band", "Dark Side Of The Moon", "Walls"))
    }

    User(identifier).deleteUsersCollection()
  }
}
