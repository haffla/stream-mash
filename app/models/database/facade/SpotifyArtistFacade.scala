package models.database.facade

import models.database.alias._
import org.squeryl.PrimitiveTypeMode._
import play.api.libs.json.{Json, JsValue}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SpotifyArtistFacade(identifier:Either[Int,String]) {

  /**
    * Get all Spotify albums of those artists that were imported by the user
    */
  def getUserRelatedSpotifyAlbums:List[(Album,Artist,String)] = {
    transaction {

      val dislikedArtists = from(AppDB.userArtistLikings)(ual => where(AppDB.userWhereClause(ual,identifier) and ual.score === 0) select ual.artistId)

      /** First get all related artist */
      val usersArtists =
          from(AppDB.artists, AppDB.collections, AppDB.tracks)((a,c,t) =>
            where(c.trackId === t.id and t.artistId === a.id and AppDB.userWhereClause(c,identifier) and a.id.notIn(dislikedArtists))
              select a.id
          )

      join(AppDB.albums,
           AppDB.artists,
           AppDB.spotifyAlbums,
           AppDB.spotifyArtists)( (alb,art,spAlb,spArt) =>
        where(art.id in usersArtists.toList)
          select(alb, art, spAlb.spotifyId)
          on(
            alb.artistId === art.id,
            alb.id === spAlb.id,
            art.id === spArt.id
          )
      ).toList
    }
  }

  def getArtistsAndAlbumsForOverview:Future[JsValue] = {
    for {
      albumsInUserCollection <- Future { AlbumFacade(identifier).getUsersAlbums }
      spotifyAlbums <- Future { getUserRelatedSpotifyAlbums }
    } yield convertToJson(spotifyAlbums,albumsInUserCollection)
  }

  private def convertToJson(albums:List[(Album,Artist,String)], albumsInUserCollection:List[Long] = Nil):JsValue = {
    val convertedToMap = albums.foldLeft(Map[Artist,Set[(String,String,Boolean)]]()) { (prev,curr) =>
      val artist = curr._2
      val currentAlbum = curr._1.name
      val albumSpotifyId = curr._3
      val userHasAlbumInCollection = albumsInUserCollection.contains(curr._1.id)
      val aggregatedAlbums = prev.getOrElse(artist, Set.empty) ++ Set((currentAlbum,albumSpotifyId,userHasAlbumInCollection))
      prev + (artist -> aggregatedAlbums)
    }
    doJsonConversion(convertedToMap)
  }

  private def doJsonConversion(artistAlbumMap: Map[Artist,Set[(String,String,Boolean)]]): JsValue = {
    val list = artistAlbumMap.map { elem =>
      val albums = elem._2.map { album =>
        Json.obj(
          "name" -> album._1,
          "id" -> album._2,
          "inCollection" -> album._3
        )
      }
      val spotifyId:String = elem._1.spotifyId.getOrElse("")
      val pic = elem._1.pic.getOrElse("")
      Json.obj(
        "name" -> elem._1.name,
        "id" -> spotifyId,
        "img" -> pic,
        "albums" -> albums
      )
    }
    Json.toJson(list)
  }

}

object SpotifyArtistFacade {

  def apply(identifier: Either[Int,String]) = new SpotifyArtistFacade(identifier)

  def saveArtistWithName(artistName:String):Long = {
    transaction {
      from(AppDB.artists)(a =>
        where(a.name === artistName)
          select a.id
      ).headOption match {
        case Some(artistId) => insertArtist(artistId)
        case _ =>
          val newArtist:Artist = AppDB.artists.insert(Artist(artistName))
          AppDB.spotifyArtists.insert(SpotifyArtist(newArtist.id)).id

      }
    }
  }

  def insertArtist(id:Long):Long = {
    from(AppDB.spotifyArtists)(sa =>
      where(sa.id === id)
        select sa.id
    ).headOption match {
      case None => AppDB.spotifyArtists.insert(SpotifyArtist(id)).id
      case _ => id
    }
  }

  /**
    * Save whatever info is needed about an artist from Spotify
    */
  def saveInfoAboutArtist(js:JsValue):Unit = {
    (js \ "images").asOpt[List[JsValue]] match {
      case Some(images) =>
        val filtered = images.filter { image =>
          val width = (image \ "width").as[Int]
          width < 1000 && width > 300
        }
        filtered.headOption.map { img =>
          val url = (img \ "url").as[String]
          ArtistFacade.setArtistPic((js \ "name").as[String], url)
        }
      case None =>
    }
  }
}
