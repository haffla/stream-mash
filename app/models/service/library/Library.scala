package models.service.library

import database.MainDatabaseAccess
import database.alias.Album
import play.api.Play
import play.api.db.slick.{HasDatabaseConfig, DatabaseConfigProvider}
import play.api.libs.json.{Json, JsObject, JsValue}
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class Library(user_id:Int) extends HasDatabaseConfig[JdbcProfile]
                                            with MainDatabaseAccess {

  import driver.api._
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  /**
   * Cleans the data by transforming the Seq[Map[String,String]]
   * to a Map[String, Set[String]]
   */
  def convertSeqToMap(data: Seq[Map[String,String]], keyArtist:String = "artist", keyAlbum:String = "album"):Map[String, Set[String]] = {
    data.foldLeft(Map[String, Set[String]]()) { (prev, curr) =>
      val artist:String = curr(keyArtist)
      val album:Option[String] = curr.get(keyAlbum)
      val artistAlbums:Set[String] = prev get artist match {
        case None => Set.empty
        case Some(albums) => albums
      }
      val added:Set[String] = album match {
        case Some(alb) => artistAlbums + alb
        case None => artistAlbums
      }
      prev + (artist -> added)
    }
  }

  /**
   * Transforms the collection to a Json Array of Json Objects
   */
  def prepareCollectionForFrontend(data:Map[String, Set[String]]):JsValue = {
    val formattedData:List[JsObject] = data.keySet.toList.map { artist =>
      val albums = data(artist).toList.map { albumName =>
        Json.obj("name" -> albumName)
      }
      Json.obj(
        "name" -> artist,
        "albums" -> Json.toJson(albums)
      )
    }
    Json.toJson(formattedData)
  }

  def persist(library: Map[String, Set[String]]):Unit = {
    library.foreach { collection =>
      val interpret = collection._1
      val albums = collection._2
      albums.foreach { album =>
        getOrSaveCollectionItem(album, interpret).onComplete {
          case Success(id) => //println(id)
          case Failure(t) => println(t.getMessage)
        }
      }
    }
  }

  def getOrSaveCollectionItem(name: String, interpret:String):Future[Int] = {
    db.run(albumQuery.filter { albums =>
      albums.name === name && albums.interpret === interpret && albums.id_user === user_id
    }.result).flatMap { albumList =>
      if (albumList.nonEmpty) Future.successful(albumList.head.id.get)
      else {
        db.run(albumQuery returning albumQuery.map(_.id) += Album(name = name, interpret = interpret, fk_user = user_id))
      }
    }
  }

  /**
    * Gets all collections (album / artist) of user from DB
    */
  def getCollectionFromDbByUser(id:Int):Future[Option[Map[String, Set[String]]]] = {
    db.run(albumQuery.filter(_.id_user === id).result.map { album =>
      if(album.isEmpty) None
      else {
        Some(
          album.foldLeft(Map[String, Set[String]]()) {(prev, curr) =>
            val interpret = curr.interpret
            val interpretAlbums:Set[String] = prev get interpret match {
              case None => Set.empty
              case Some(albums) => albums
            }
            val added:Set[String] = interpretAlbums + curr.name
            prev + (interpret -> added)
          }
        )
      }
    })
  }

}
