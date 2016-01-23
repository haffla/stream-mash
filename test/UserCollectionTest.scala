
import models.User
import models.database.facade.CollectionFacade
import models.service.library._
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.libs.json.JsValue
import play.api.test.WithApplication

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class UserCollectionTest extends UnitSpec {

  implicit val defaultPatience = PatienceConfig(timeout = Span(4, Seconds), interval = Span(500, Millis))

  val identifier = Right("testusersession")

  "The Library" should "correctly save a collection to DB and retrieve it" in new WithApplication {
    val collection = Map(
      "The Beatles" -> Map("Sgt. Pepper’s Lonely Hearts Club Band" -> Set("With a Little Help from My Friends", "Getting Better")),
      "Extrawelt" -> Map("Dark Side Of The Moon" -> Set("Etre", "Colomb")),
      "Apparat" -> Map("Walls" -> Set("Useless Information", "Limelight"))
    )
    val library = new Library(identifier)
    library.persist(collection).onComplete {
      case Success(_) =>
        val fromDb = CollectionFacade(identifier).userCollection
        whenReady(fromDb) { col =>
          val forFrontEnd = library.prepareCollectionForFrontend(col)

          val converted: List[JsValue] = forFrontEnd.as[List[JsValue]]
          val artists = converted map (x => (x \ "name").as[String])
          val ratings = converted map (x => (x \ "rating").as[Int])
          val trackCounts = converted map (x => (x \ "trackCount").as[Int])
          val albumObjects = converted map (x => (x \ "albums").as[List[JsValue]])

          val albums = albumObjects flatMap { albumObj =>
            albumObj map { album =>
              (album \ "name").as[String]
            }
          }

          val tracks: List[String] = albumObjects flatMap { albumObj =>
            albumObj flatMap { album =>
              (album \ "tracks").as[Set[JsValue]].map { tr =>
                (tr \ "name").as[String]
              }
            }
          }

          ratings should equal(List(1,1,1))
          trackCounts should equal(List(2,2,2))
          tracks.toSet should equal(Set("With a Little Help from My Friends", "Getting Better", "Etre", "Colomb", "Useless Information", "Limelight"))
          artists.toSet should equal(Set("The Beatles", "Apparat", "Extrawelt"))
          albums.toSet should equal(Set("Sgt. Pepper’s Lonely Hearts Club Band", "Dark Side Of The Moon", "Walls"))
        }

        User(identifier).deleteUsersCollection()

      case Failure(e) =>
        e.printStackTrace()
        fail("Persisting the data failed")
    }
  }
}
