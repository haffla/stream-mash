package models.database.facade.service

import models.database.AppDB
import models.database.alias._
import models.database.alias.service.DeezerArtist
import models.database.facade.ArtistFacade
import models.util.Constants
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.GroupWithMeasures
import play.api.libs.json.JsValue

class DeezerArtistFacade(identifier:Either[Int,String]) extends ServiceArtistFacade(identifier, Constants.serviceDeezer) {

  override def artistsAndAlbums(usersArtists:List[Long]):List[(Album,Artist,String)] = {
    join(AppDB.albums,
         AppDB.artists,
         AppDB.deezerAlbums,
         AppDB.deezerArtists,
         AppDB.userArtistLikings)( (alb,art,deeAlb,deeArt,ual) =>
      where(art.id in usersArtists and ual.score.gt(0) and AppDB.userWhereClause(ual,identifier))
        select(alb, art, deeAlb.deezerId)
        on(
        alb.artistId === art.id,
        alb.id === deeAlb.id,
        art.id === deeArt.id,
        art.id === ual.artistId
        )
    ).toList
  }
}

object DeezerArtistFacade extends ServiceArtistTrait {

  override def apply(identifier: Either[Int,String]) = new DeezerArtistFacade(identifier)

  override protected def setArtistAnalysed(id: Long) = {
    update(AppDB.deezerArtists)(d =>
      where(d.id === id)
      set(d.isAnalysed := true)
    )
  }

  override protected def insertOrUpdate(id:Long):Long = {
    from(AppDB.deezerArtists)(da =>
      where(da.id === id)
        select da
    ).headOption match {
      case None => insert(id)
      case Some(deArt) =>
        if (!deArt.isAnalysed)
          setArtistAnalysed(deArt.id)
        deArt.id
    }
  }

  override protected def insert(id: Long):Long = {
    AppDB.deezerArtists.insert(DeezerArtist(id, isAnalysed = true)).id
  }

  /**
    * Save whatever info is needed about an artist from Spotify
    */
  def saveInfoAboutArtist(js:JsValue):Unit = {
    (js \ "picture_big").asOpt[String] match {
      case Some(picture) =>
        ArtistFacade.setArtistPic((js \ "name").as[String], picture)
      case None =>
    }
  }

  override def analysedArtistIds(artistIds: List[Long]): List[Long] = {
    transaction {
      from(AppDB.deezerArtists)(da =>
        where(da.isAnalysed === true and da.id.in(artistIds))
        select da.id
      ).toList
    }
  }

  override def artistsAlbumCount(artistId:List[Long]): (String, List[GroupWithMeasures[Long,Long]]) = {
    val res = from(AppDB.deezerArtists, AppDB.deezerAlbums, AppDB.albums)((deArt,deAlb,alb) =>
      where(deArt.id in artistId and alb.id === deAlb.id and alb.artistId === deArt.id)
        groupBy deArt.id
        compute countDistinct(alb.id)
    ).toList

    (Constants.serviceDeezer, res)
  }
}


