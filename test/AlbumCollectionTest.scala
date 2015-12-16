import models.database.facade.AlbumFacade
import models.service.library._
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.JsValue
import play.api.test.WithApplication

class AlbumCollectionTest extends UnitSpec with ScalaFutures {

  val identifier = Right("testusersession")

  "Creating albums, saving them to DB and retrieving them" should "work" in new WithApplication {
    val albums = Map(
      "The Beatles" -> Set("Sgt. Pepper’s Lonely Hearts Club Band", "Abbey Road"),
      "Extrawelt" -> Set("Dark Side Of The Moon"),
      "Apparat" -> Set("Walls")
    )

    val library = new Library(identifier)
    library.persist(albums)

    Thread.sleep(1000) // Need to wait a little for the data to be saved in DB

    val fromDb = AlbumFacade(identifier).getUsersAlbumCollection
    whenReady(fromDb) { collection =>
      val col = collection.getOrElse(Map.empty)
      val forFrontEnd = library.prepareCollectionForFrontend(col)

      val converted:List[JsValue] = forFrontEnd.as[List[JsValue]]
      val names = converted map(x => (x \ "name").as[String])

      val albums = converted map(x => (x \ "albums").as[List[JsValue]]) flatMap { albumSet =>
        albumSet map { album =>
          (album \ "name").as[String]
        }
      }

      names.toSet should equal(Set("The Beatles", "Apparat", "Extrawelt"))
      albums.toSet should equal(Set("Abbey Road", "Sgt. Pepper’s Lonely Hearts Club Band", "Dark Side Of The Moon", "Walls"))
    }

    val futureDelete = AlbumFacade(identifier).deleteUsersAlbums()
    whenReady(futureDelete) { countDeletedRows =>
      countDeletedRows should equal(4)
    }

  }
}
