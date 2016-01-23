import models.database.facade._
import models.database.facade.service._
import models.util.TextWrangler
import org.scalatest.time.{Millis, Seconds, Span}
import org.squeryl.PrimitiveTypeMode._
import play.api.libs.json.JsValue
import play.api.test.WithApplication

class AbTest extends UnitSpec {

  implicit val defaultPatience = PatienceConfig(timeout = Span(8, Seconds), interval = Span(500, Millis))

  val id = Right("someusersession")

  val alf = ArtistLikingFacade(id)
  val collectionFacade = CollectionFacade(id)

  "Do something" should "adasd" in new WithApplication {
    using(squerylSession) {
      val namaste = ArtistFacade.insert("Namaste", alf)
      val hola = ArtistFacade.insert("Hola", alf)
      val champ = ArtistFacade.insert("Champ", alf)
      val hakuna = ArtistFacade.insert("Hakunamatata", alf)
      val held = ArtistFacade.insert("Held", alf)

      val namasteAlbOne = AlbumFacade.insert("Namamu1", namaste)
      val namasteAlbTwo = AlbumFacade.insert("Namamu2", namaste)
      val holaAlb = AlbumFacade.insert("Hick Hock", hola)
      val champAlb = AlbumFacade.insert("Champions of Titikaka", champ)
      val champAlbTwo = AlbumFacade.insert("Champions of Titikaka Two", champ)
      val hakunaAlb = AlbumFacade.insert("Balu", hakuna)
      val heldAlb = AlbumFacade.insert("Hero", held)

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

      alf.setScoreForArtist("Namaste", 0)
      alf.setScoreForArtist("Held", 0)
      alf.setScoreForArtist("Hakunamatata", 3)

      List(namaste, hola, champ, hakuna, held).foreach { art =>
        SpotifyArtistFacade.saveArtist(art)
        DeezerArtistFacade.saveArtist(art)
        NapsterArtistFacade.saveArtist(art)
      }

      List(
        ("Namamu1", namaste),
        ("Namamu2", namaste),
        ("Hick Hock", hola),
        ("Champions of Titikaka", champ),
        ("Balu", hakuna),
        ("Hero", held)
      ).foreach { alb =>
        SpotifyAlbumFacade.saveAlbumWithNameAndId(alb._1, alb._2, TextWrangler.generateRandomString(10))
        DeezerAlbumFacade.saveAlbumWithNameAndId(alb._1, alb._2, TextWrangler.generateRandomString(10))
        NapsterAlbumFacade.saveAlbumWithNameAndId(alb._1, alb._2, TextWrangler.generateRandomString(10))
      }

      val spotifyResult = SpotifyArtistFacade(id).getArtistsAndAlbumsForOverview
      val deezerResult = DeezerArtistFacade(id).getArtistsAndAlbumsForOverview
      val napsterResult = NapsterArtistFacade(id).getArtistsAndAlbumsForOverview
      whenReady(spotifyResult) { spRes =>
        val artists = (spRes \ "artists").as[List[JsValue]]
        val stats = (spRes \ "stats").as[JsValue]
        (stats \ "nrUserAlbums").as[Int] should be(4)
        (stats \ "nrAlbums").as[Int] should be(3)
        (stats \ "nrArtists").as[Int] should be(3)
        artists.length should be(3)
      }
      whenReady(deezerResult) { deeRes =>
        val artists = (deeRes \ "artists").as[List[JsValue]]
        val stats = (deeRes \ "stats").as[JsValue]
        (stats \ "nrUserAlbums").as[Int] should be(4)
        (stats \ "nrAlbums").as[Int] should be(3)
        (stats \ "nrArtists").as[Int] should be(3)
        artists.length should be(3)
      }
      whenReady(napsterResult) { napsRes =>
        val artists = (napsRes \ "artists").as[List[JsValue]]
        val stats = (napsRes \ "stats").as[JsValue]
        (stats \ "nrUserAlbums").as[Int] should be(4)
        (stats \ "nrAlbums").as[Int] should be(3)
        (stats \ "nrArtists").as[Int] should be(3)
        artists.length should be(3)
      }

    }

  }
}
