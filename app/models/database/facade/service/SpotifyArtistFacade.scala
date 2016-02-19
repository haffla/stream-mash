package models.database.facade.service

import models.database.AppDB
import models.database.alias._
import models.database.alias.service.SpotifyArtist
import models.database.facade.ArtistFacade
import models.util.Constants
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.GroupWithMeasures
import play.api.libs.json.JsValue

class SpotifyArtistFacade(identifier:Either[Int,String]) extends ServiceArtistFacade(identifier, Constants.serviceSpotify) {

  /**
    * Each streaming artist facade needs to implement this method. Join artists and albums
    * with the respective streaming service artists and albums.
    */
  override def artistsAndAlbums(usersArtists:List[Long]):List[(Album,Artist,String)] = {
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

  override def apply(identifier: Either[Int,String]) = new SpotifyArtistFacade(identifier)

  override protected def setArtistAnalysed(id: Long) = {
    update(AppDB.spotifyArtists)(s =>
      where(s.id === id)
      set(s.isAnalysed := true)
    )
  }

  override protected def insertOrUpdate(id:Long):Long = {
    from(AppDB.spotifyArtists)(sa =>
      where(sa.id === id)
        select sa
    ).headOption match {
      case Some(artist) =>
        if (!artist.isAnalysed)
          setArtistAnalysed(artist.id)
        artist.id
      case _ => insert(id)

    }
  }

  override protected def insert(id: Long):Long = {
    AppDB.spotifyArtists.insert(SpotifyArtist(id, isAnalysed = true)).id
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

  override def analysedArtistIds(artistIds: List[Long]): List[Long] = {
    inTransaction {
      from(AppDB.spotifyArtists)(spArt =>
        where(spArt.isAnalysed === true and spArt.id.in(artistIds))
        select spArt.id
      ).toList
    }
  }

  override def artistsAlbumCount(artistId:List[Long]): (String, List[GroupWithMeasures[Long,Long]]) = {
    val res = from(AppDB.albums, AppDB.spotifyArtists, AppDB.spotifyAlbums)((alb,spArt,spAlb) =>
      where(spArt.id in artistId and alb.id === spAlb.id and alb.artistId === spArt.id)
        groupBy spArt.id
        compute countDistinct(alb.id)
      ).toList

    (Constants.serviceSpotify, res)
  }
}
