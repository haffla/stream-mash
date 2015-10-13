package controllers

import java.io.File
import java.nio.file.Files
import models.User
import models.auth.MessageDigest

import scala.concurrent.Future

import models.util.{ArtistLibrary, ItunesLibrary}
import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class ItunesController extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.itunes.index())
  }

  def artistAlbumCollectionFromDb = Action.async { implicit request =>
    val userId =request.session.get("user_id").get.toInt
    collectionFromDb(userId)
  }

  def fileUpload = Action.async(parse.multipartFormData) { implicit request =>
    request.body.file("file").map { file =>
      val userId:Int = request.session.get("user_id").get.toInt
      val filename = file.filename
      val username = request.session.get("username")
        .getOrElse("user-" + System.currentTimeMillis)
      val xmlPath = s"/tmp/$filename$username"
      file.ref.moveTo(new File(xmlPath))
      val f = new File(xmlPath)
      val fileBody:String = scala.io.Source.fromFile(f).getLines().mkString
      val fileHash = MessageDigest.md5(fileBody)
      User.iTunesFileProcessedAlready(userId,fileHash).flatMap(
       bool => if(bool) {
         //user has submitted the exact same file. load from db.
         cleanUp(f)
         collectionFromDb(userId)
       } else {
         User.saveItunesFileHash(userId, fileHash)
         val json = collectionFromXml(userId, xmlPath)
         cleanUp(f)
         Future.successful(Ok(json))
       })
    }.getOrElse {
      val jsonResponse = Json.toJson(Map("error" -> "Could not read the file"))
      Future.successful(Ok(jsonResponse))
    }
  }

  def cleanUp(f:File) = {
    Files.delete(f.toPath)
  }

  def collectionFromDb(userId:Int) = {
    val library = new ItunesLibrary(userId)
    library.getCollectionFromDbByUser(userId).map {
      case Some(collection) => Ok(Json.toJson(collection))
      case None => Ok(Json.toJson(Map("error" -> "You have no records stored in our database.")))
    }
  }

  def collectionFromXml(userId:Int,xmlPath:String) = {
    val library = new ItunesLibrary(userId, xmlPath)
    val collection = library.getCollection
    Json.toJson(collection)
  }

  def getSpotifyArtistId = Action.async { implicit request =>
    val artist = request.getQueryString("artist").get
    ArtistLibrary.getIdForArtistFromDb(artist).flatMap {
        case Some(spotifyId) => Future.successful(Ok(Json.toJson(Map("spotify_id" -> spotifyId))))
        case None =>
          val id:Future[Option[String]] = ArtistLibrary.getIdForArtistFromSpotify(artist)
          id.map {
            case Some(sp) => Ok(Json.toJson(Map("spotify_id" -> sp)))
            case None => Ok(Json.toJson(Map("error" -> "Did not find a Spotify ID")))
          }
    }
  }
}
