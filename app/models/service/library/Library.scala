package models.service.library

import com.rabbitmq.client.MessageProperties
import models.database.MainDatabaseAccess
import models.database.alias.Album
import models.Config
import models.messaging.RabbitMQConnection
import play.api.Play
import play.api.db.slick.{HasDatabaseConfig, DatabaseConfigProvider}
import play.api.libs.json.{Json, JsObject, JsValue}
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class Library(userId:Int) extends HasDatabaseConfig[JdbcProfile]
                                            with MainDatabaseAccess {

  import driver.api._
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  /**
   * Cleans the data by transforming the Seq[Map[String,String]]
   * to a Map[String, Set[String]]
   */
  def convertSeqToMap(data: Seq[Map[String,String]], keyArtist:String = "artist", keyAlbum:String = "album"):Map[String, Set[String]] = {
    val result = data.foldLeft(Map[String, Set[String]]()) { (prev, curr) =>
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
    persist(result)
    result
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

  private def getOrSaveCollectionItem(name: String, interpret:String):Future[Int] = {
    db.run(albumQuery.filter { albums =>
      albums.name === name && albums.interpret === interpret && albums.id_user === userId
    }.result).flatMap { albumList =>
      if (albumList.nonEmpty) Future.successful(albumList.head.id.get)
      else {
        db.run(albumQuery returning albumQuery.map(_.id) += Album(name = name, interpret = interpret, fk_user = userId))
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

  def pushToArtistIdQueue(name: String, id: String, typ:String) = {
    val connection = RabbitMQConnection.getConnection()
    val channel = connection.createChannel()
    channel.queueDeclare(Config.rabbitArtistIdQueue, true, false, false, null)
    val message = Json.toJson(Map("name" -> name, "id" -> id, "type" -> typ)).toString()
    channel.basicPublish("", Config.rabbitArtistIdQueue, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes)
    channel.close()
    connection.close()
  }

}
