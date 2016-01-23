package models.database.facade.service

import models.database.alias._
import models.database.alias.service.SpotifyArtist
import models.database.facade.ArtistFacade
import models.service.Constants
import org.squeryl.PrimitiveTypeMode._
import play.api.libs.json.JsValue

class SpotifyArtistFacade(identifier:Either[Int,String]) extends ServiceArtistFacade(identifier) {

  val serviceName = Constants.serviceSpotify

  /**
    * Each streaming artist facade needs to implement this method. Join artists and albums
    * with the respective streaming service artists and albums.
    */
  override protected def joinWithArtistsAndAlbums(usersArtists:List[Long]):List[(Album,Artist,String)] = {
    join(AppDB.albums,
         AppDB.artists,
         AppDB.spotifyAlbums,
         AppDB.spotifyArtists,
         AppDB.userArtistLikings)( (alb,art,spAlb,spArt,ual) =>
      where(art.id in usersArtists and ual.score.gt(0) and AppDB.userWhereClause(ual,identifier))
        select(alb, art, spAlb.spotifyId)
        on(
        alb.artistId === art.id,
        alb.id === spAlb.id,
        art.id === spArt.id,
        art.id === ual.artistId
        )
    ).toList
  }

}

object SpotifyArtistFacade extends ServiceArtistTrait {

  def apply(identifier: Either[Int,String]) = new SpotifyArtistFacade(identifier)

  override protected def insertIfNotExists(id:Long):Long = {
    from(AppDB.spotifyArtists)(sa =>
      where(sa.id === id)
        select sa.id
    ).headOption match {
      case None => insert(id)
      case _ => id
    }
  }

  override protected def insert(id: Long):Long = {
    AppDB.spotifyArtists.insert(SpotifyArtist(id)).id
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

  override def allArtistIds: List[Long] = {
    transaction {
      from(AppDB.spotifyArtists)(da => select(da.id)).toList
    }
  }
}
