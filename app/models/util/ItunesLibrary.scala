package models.util

import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import play.api.libs.json._
import slick.driver.JdbcProfile
import database.MainDatabaseAccess
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.util.{Success, Failure}
import scala.xml.Node

class ItunesLibrary(user_id:Int, xmlPath:String = "") extends HasDatabaseConfig[JdbcProfile]
                                           with MainDatabaseAccess {
  val labelDict = "dict"
  val labelKey  = "key"
  val informationToExtract = List("Artist", "Album")
  val minTupleLength = informationToExtract.length

  import driver.api._
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  /**
   * Parses the Itunes Library XML file and returns all songs
   * as a sequence of maps
   */
  private def parseXml:Seq[Map[String,String]] = {
    val xml = scala.xml.XML.loadFile(xmlPath)
    val dict = xml \ labelDict \ labelDict \ labelDict
    dict.map { d =>
      val keys = (d \ labelKey).toList
      val other = (d \ "_").toList.filter(x => x.label != labelKey)
      val zp:List[(Node,Node)] = keys.zip(other)
      zp.filter(informationToExtract contains _._1.text)
        .map {
        x => (x._1.text,x._2.text)
      }.toMap
    }.filter(_.size >= minTupleLength)
  }

  private def persist(library: Map[String, Set[String]]):Unit = {
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

  private def getOrSaveCollectionItem(name: String, interpret:String):Future[Int] = {
    db.run(albumQuery.filter { albums =>
      albums.name === name && albums.interpret === interpret && albums.id_user === user_id
    }.result).flatMap { albumList =>
      if (albumList.nonEmpty) Future.successful(albumList.head.id.get)
      else {
        db.run(albumQuery returning albumQuery.map(_.id) += models.music.Album(name = name, interpret = interpret, fk_user = user_id))
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

  /**
   * Transforms the result of the parsed xml which is Seq[Map[String,String]]
   * to a Map[String, Set[String]]
   */
  def getCollection: Map[String, Set[String]] = {
    val lib:Seq[Map[String,String]] = parseXml
    val library = lib.foldLeft(Map[String, Set[String]]()) {(prev, curr) =>
      val artist:String = curr(informationToExtract.head)
      val album:String = curr(informationToExtract(1))
      val artistAlbums:Set[String] = prev get artist match {
        case None => Set.empty
        case Some(albums) => albums
      }
      val added:Set[String] = artistAlbums + album
      prev + (artist -> added)
    }
    persist(library)
    library
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
}
