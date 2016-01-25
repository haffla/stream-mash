import models.database.facade._
import models.database.facade.service._
import models.util.{Constants, TextWrangler}
import org.scalatest.time.{Millis, Seconds, Span}
import org.squeryl.PrimitiveTypeMode._
import play.api.libs.json.JsValue
import play.api.test.WithApplication

class ServiceFacadeTest extends UnitSpec {

  implicit val defaultPatience = PatienceConfig(timeout = Span(8, Seconds), interval = Span(500, Millis))

  val id = Right("someusersession")

  val alf = ArtistLikingFacade(id)
  val collectionFacade = CollectionFacade(id)

  val namasteArtist = "Namaste"
  val holaArtist = "Hola"
  val champArtist = "Champ"
  val hakuArtist = "Hakunamatata"
  val heldArtist = "Held"

  val namaAlbOne = "Namamu1"
  val namaAlbTwo = "Namamu2"
  val hickAlbum = "Hick Hock"
  val champAlbumOne = "Champions of Titikaka"
  val champAlbumTwo = "Champions of Titikaka Two"
  val baluAlb = "Balu"
  val heroAlb = "Hero"

  "The Service Facades" should "should return expected data" in new WithApplication {
    using(squerylSession) {
      val namaste = ArtistFacade.insert(namasteArtist, alf)
      val hola = ArtistFacade.insert(holaArtist, alf)
      val champ = ArtistFacade.insert(champArtist, alf)
      val hakuna = ArtistFacade.insert(hakuArtist, alf)
      val held = ArtistFacade.insert(heldArtist, alf)

      val namasteAlbOne = AlbumFacade.insert(namaAlbOne, namaste)
      val namasteAlbTwo = AlbumFacade.insert(namaAlbTwo, namaste)
      val holaAlb = AlbumFacade.insert(hickAlbum, hola)
      val champAlb = AlbumFacade.insert(champAlbumOne, champ)
      val champAlbTwo = AlbumFacade.insert(champAlbumTwo, champ)
      val hakunaAlb = AlbumFacade.insert(baluAlb, hakuna)
      val heldAlb = AlbumFacade.insert(heroAlb, held)

      val namaTrackOne = TrackFacade.insert("N1 Track", namaste, namasteAlbOne)
      val namaTrackTwo = TrackFacade.insert("N2 Track", namaste, namasteAlbTwo)
      val holaTrack = TrackFacade.insert("Hola Na", hola, holaAlb)
      val champTrack = TrackFacade.insert("I am Legend", champ, champAlb)
      val champTrackTwo = TrackFacade.insert("I am Legend", champ, champAlbTwo)
      val hakunaTrackOne = TrackFacade.insert("My friend Shirkan", hakuna, hakunaAlb)
      val hakunaTrackTwo = TrackFacade.insert("I love Mogli", hakuna, hakunaAlb)
      val hakunaTrackThree = TrackFacade.insert("Dancing with Balu", hakuna, hakunaAlb)
      val hakunaTrackFour = TrackFacade.insert("Cuddling with Kaa", hakuna, hakunaAlb)
      val hakunaTrackFive = TrackFacade.insert("King Louie The King", hakuna, hakunaAlb)
      val heldTrack = TrackFacade.insert("Wir waren Helden", held, heldAlb)

      List(namaTrackOne, namaTrackTwo, holaTrack, champTrack, champTrackTwo, hakunaTrackOne, hakunaTrackTwo, hakunaTrackThree,
        hakunaTrackFour, hakunaTrackFive, heldTrack).foreach { tr =>
        collectionFacade.insert(tr)
      }

      alf.setScoreForArtist(namasteArtist, 0)
      alf.setScoreForArtist(heldArtist, 0)
      alf.setScoreForArtist(hakuArtist, 3)

      List(namaste, hola, champ, hakuna, held).foreach { art =>
        SpotifyArtistFacade.saveArtist(art)
        DeezerArtistFacade.saveArtist(art)
        NapsterArtistFacade.saveArtist(art)
      }

      List(
        (namaAlbOne, namaste),
        (namaAlbTwo, namaste),
        (hickAlbum, hola),
        (champAlbumOne, champ),
        (baluAlb, hakuna),
        (heroAlb, held)
      ).foreach { alb =>
        SpotifyAlbumFacade.saveAlbumWithNameAndId(alb._1, alb._2, TextWrangler.generateRandomString(10))
        DeezerAlbumFacade.saveAlbumWithNameAndId(alb._1, alb._2, TextWrangler.generateRandomString(10))
        NapsterAlbumFacade.saveAlbumWithNameAndId(alb._1, alb._2, TextWrangler.generateRandomString(10))
      }

      val spotifyResult = SpotifyArtistFacade(id).getArtistsAndAlbumsForOverview
      val deezerResult = DeezerArtistFacade(id).getArtistsAndAlbumsForOverview
      val napsterResult = NapsterArtistFacade(id).getArtistsAndAlbumsForOverview
      whenReady(spotifyResult) { spRes =>
        val artists = (spRes \ Constants.jsonKeyArtists).as[List[JsValue]]
        val stats = (spRes \ Constants.jsonKeyStats).as[JsValue]
        (stats \ Constants.jsonKeyNrUserAlbs).as[Int] should be(4)
        (stats \ Constants.jsonKeyNrAlbs).as[Int] should be(3)
        (stats \ Constants.jsonKeyNrArts).as[Int] should be(3)
        artists.length should be(3)
      }
      whenReady(deezerResult) { deeRes =>
        val artists = (deeRes \ Constants.jsonKeyArtists).as[List[JsValue]]
        val stats = (deeRes \ Constants.jsonKeyStats).as[JsValue]
        (stats \ Constants.jsonKeyNrUserAlbs).as[Int] should be(4)
        (stats \ Constants.jsonKeyNrAlbs).as[Int] should be(3)
        (stats \ Constants.jsonKeyNrArts).as[Int] should be(3)
        artists.length should be(3)
      }
      whenReady(napsterResult) { napsRes =>
        val artists = (napsRes \ Constants.jsonKeyArtists).as[List[JsValue]]
        val stats = (napsRes \ Constants.jsonKeyStats).as[JsValue]
        (stats \ Constants.jsonKeyNrUserAlbs).as[Int] should be(4)
        (stats \ Constants.jsonKeyNrAlbs).as[Int] should be(3)
        (stats \ Constants.jsonKeyNrArts).as[Int] should be(3)
        artists.length should be(3)
      }

    }

  }
}
