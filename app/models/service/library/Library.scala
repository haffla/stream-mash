package models.service.library

import models.service.Constants
import models.service.api.discover.RetrievalProcessMonitor
import play.api.libs.json.{JsValue, Json}
import models.database.alias.{Album,Artist,Track}

import scalikejdbc._

class Library(identifier: Either[Int, String], name:String = "", persist:Boolean = true) {

  implicit val session = AutoSession

  val apiHelper = new RetrievalProcessMonitor(name, identifier)

  /**
   * Cleans the data by transforming the Seq[Map[String,String]]
   * to a Map[String, Map[String,Set[String]]]
   * The artist key maps to a map of which the keys are the album titles
   * and the values are sets of track names
   */
  def convertSeqToMap(data: Seq[Map[String,String]],
                      keyArtist:String = Constants.mapKeyArtist,
                      keyAlbum:String = Constants.mapKeyAlbum,
                      keyTrack:String = Constants.mapKeyTrack):Map[String, Map[String,Set[String]]] = {

    val result = data.foldLeft(Map[String, Map[String,Set[String]]]()) { (prev, curr) =>
      val artist:String = curr(keyArtist)
      val album:Option[String] = curr.get(keyAlbum)
      val track:Option[String] = curr.get(keyTrack)

      val artistAlbums:Map[String,Set[String]] = prev.get(artist) match {
        case None => Map.empty
        case Some(albums) => albums
      }

      val aggregated:Map[String, Set[String]] = (album,track) match {
        case (Some(alb), Some(tr)) =>
          artistAlbums.get(alb) match {
            case Some(existingTracks) =>
              val tracks:Set[String] = existingTracks + tr
              artistAlbums + (alb -> tracks)
            case None =>
              artistAlbums + (alb -> Set(tr))
          }
        case (Some(alb), None) =>
          artistAlbums.get(alb) match {
            case Some(_) =>
              artistAlbums
            case None =>
              artistAlbums + (alb -> Set.empty)
          }
        case (None, Some(tr)) =>
          artistAlbums.get(Constants.mapKeyUnknownAlbum) match {
            case Some(existingTracks) =>
              val tracks = existingTracks + tr
              artistAlbums + (Constants.mapKeyUnknownAlbum -> tracks)
            case None =>
              artistAlbums + (Constants.mapKeyUnknownAlbum -> Set(tr))
          }
        case (None,None) =>
          artistAlbums
      }

      prev + (artist -> aggregated)
    }
    if(persist) persist(result)
    result
  }

  /**
   * Transforms the collection to a Json Array of Json Objects
   */
  def prepareCollectionForFrontend(data:List[(Album,Artist,Track)]):JsValue = {
    val converted = convert(data)
    val jsObjects = converted.map { artist =>
      val artistName:String = artist._1
      val albums:Map[String,Set[String]] = artist._2
      val albumObjects = albums.map { album =>
        val albumName:String = album._1
        val tracks:Set[String] = album._2
        Json.obj(
          "name" -> albumName,
          "tracks" -> tracks
        )
      }
      Json.obj(
        "name" -> artistName,
        "albums" -> albumObjects
      )
    }
    Json.toJson(jsObjects)
  }

  private def convert(data:List[(Album,Artist,Track)]):Map[String, Map[String,Set[String]]] = {
    data.foldLeft(Map[String, Map[String,Set[String]]]()) { (prev, curr) =>
      val artist = curr._2.name
      val album = curr._1.name
      val track = curr._3.name
      val albums:Map[String,Set[String]] = prev.getOrElse(artist, Map.empty)
      val tracks:Set[String] = albums.getOrElse(album, Set.empty) + track
      val added:Map[String,Set[String]] = albums + (album -> tracks)
      prev + (artist -> added)
    }
  }

  def persist(library: Map[String, Map[String,Set[String]]]):Unit = {
    val fkUserField:SQLSyntax = identifier match {
      case Left(_) => sqls"fk_user"
      case Right(_) => sqls"user_session"
    }
    val userId = identifier match {
      case Left(id) => id
      case Right(sessionKey) => sessionKey
    }
    val totalLength = library.size
    var position = 1.0

    apiHelper.setRetrievalProcessProgress(0.66 + position / totalLength / 3)

    for((artist,albums) <- library) {
      val existingArtistId:Long = sql"select id_artist from artist where artist_name = $artist".map(rs => rs.long("id_artist")).single().apply() match {
        case Some(rowId) => rowId
        case None => sql"insert into artist (artist_name) values ($artist)".updateAndReturnGeneratedKey().apply()
      }

      for((album,tracks) <- albums) {
        val existingAlbumId:Long = sql"select id_album from album where album_name = $album and fk_artist = $existingArtistId".map(rs => rs.long("id_album")).single().apply() match {
          case Some(rowId) => rowId
          case None => sql"insert into album (album_name, fk_artist) values ($album, $existingArtistId)".updateAndReturnGeneratedKey().apply()
        }

        for(track <- tracks) {
          position = position + 1.0
          val trackId:Long = sql"select id_track from track where track_name = $track and fk_artist = $existingArtistId and fk_album = $existingAlbumId".map(rs => rs.long("id_track")).single().apply() match {
            case None => sql"insert into track (track_name, fk_artist, fk_album) values ($track, $existingArtistId, $existingAlbumId)".updateAndReturnGeneratedKey().apply()
            case Some(rowId) => rowId
          }

          sql"select id_collection from user_collection where $fkUserField = $userId and fk_track = $trackId".map(rs => rs.long("id_collection")).single().apply() match {
            case None => sql"insert into user_collection (fk_track, $fkUserField) values ($trackId, $userId)".update().apply()
            case Some(_) =>
          }
        }
      }
    }
  }
}
