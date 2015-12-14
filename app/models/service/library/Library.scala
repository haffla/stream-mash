package models.service.library

import com.rabbitmq.client.MessageProperties
import models.Config
import models.database.MainDatabaseAccess
import models.database.alias.Album
import models.database.facade.AlbumFacade
import models.messaging.RabbitMQConnection
import models.service.api.discover.RetrievalProcessMonitor
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import play.api.libs.json.{JsObject, JsValue, Json}
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

import scalikejdbc._

class Library(identifier: Either[Int, String], name:String = "", persist:Boolean = true) extends HasDatabaseConfig[JdbcProfile]
                                            with MainDatabaseAccess {

  implicit val session = AutoSession
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val apiHelper = new RetrievalProcessMonitor(name, identifier)

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
    if(persist) persist(result)
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
    val fkUserField:SQLSyntax = identifier match {
      case Left(_) => sqls"fk_user"
      case Right(_) => sqls"user_session_key"
    }
    val id = identifier match {
      case Left(id) => id
      case Right(sessionKey) => sessionKey
    }
    val totalLength = library.size
    var position = 1.0
    library.foreach { collection =>
      apiHelper.setRetrievalProcessProgress(0.66 + position / totalLength / 3)
      position = position + 1.0
      val interpret = collection._1
      val albums = collection._2
      albums.foreach { album =>
        sql"select * from album where name=$album and interpret=$interpret".toMap().first().apply() match {
          case Some(res) =>
          case None => sql"insert into album (name, interpret, $fkUserField) values ($album, $interpret, $id)".update().apply()
        }
      }
    }
  }

  /**
    * Gets all collections (album / artist) of user from DB
    */
  def getUsersAlbumCollection:Future[Option[Map[String, Set[String]]]] = {
    import driver.api._
    val query = identifier match {
      case Left(userId) =>
        albumQuery.filter(_.idUser === userId)
      case Right(sessionKey) =>
        albumQuery.filter(_.userSessionKey === sessionKey)
    }
    db.run(query.result map { album =>
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
