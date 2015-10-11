package models.util

import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.driver.JdbcProfile
import tables.AccountTable
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.util.{Success, Failure}
import scala.xml.Node

class ItunesLibrary(user_id:Int, xmlPath:String = "") extends HasDatabaseConfig[JdbcProfile]
                                           with AccountTable {
  val LABEL_DICT = "dict"
  val LABEL_KEY  = "key"
  val informationToExtract = List("Artist", "Album")
  val MIN_TUPLE_LENGTH = informationToExtract.length

  import driver.api._
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  /**
   * parses the Itunes Library XML file and returns all songs
   * as a sequence of maps
   */
  def parseXml:Seq[Map[String,String]] = {
    val xml = scala.xml.XML.loadFile(xmlPath)
    val dict = xml \ LABEL_DICT \ LABEL_DICT \ LABEL_DICT
    dict.map { d =>
      val keys = (d \ LABEL_KEY).toList
      val other = (d \ "_").toList.filter(x => x.label != LABEL_KEY)
      val zp:List[(Node,Node)] = keys.zip(other)
      zp.filter(informationToExtract contains _._1.text)
        .map {
        x => (x._1.text,x._2.text)
      }.toMap
    }.filter(_.size >= MIN_TUPLE_LENGTH)
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
    db.run(albumQuery.filter { album =>
      album.name === name && album.interpret === interpret && album.id_user === user_id
    }.result).flatMap { album =>
      if (album.nonEmpty) Future.successful(album.head.id.get)
      else
        db.run(albumQuery returning albumQuery.map(_.id) += models.music.Album(name = name, interpret = interpret, fk_user = user_id))
    }
  }

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

  def getCollection: Map[String, Set[String]] = {
    val lib = parseXml
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
}
