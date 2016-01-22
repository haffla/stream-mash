package models.database.facade.service

import models.database.alias._
import models.database.alias.service.DeezerArtist
import models.database.facade.ArtistFacade
import org.squeryl.PrimitiveTypeMode._
import play.api.libs.json.JsValue

class DeezerArtistFacade(identifier:Either[Int,String]) extends ServiceArtistFacade(identifier) {

  val serviceName = "deezer"

  override protected def joinWithArtistsAndAlbums(usersArtists:List[Long]):List[(Album,Artist,String)] = {
    join(AppDB.albums,
         AppDB.artists,
         AppDB.deezerAlbums,
         AppDB.deezerArtists,
         AppDB.userArtistLikings.leftOuter)( (alb,art,deeAlb,deeArt,ual) =>
      where(art.id in usersArtists and (ual.map(_.score).isNull or ual.map(_.score).gt(0)))
        select(alb, art, deeAlb.deezerId)
        on(
        alb.artistId === art.id,
        alb.id === deeAlb.id,
        art.id === deeArt.id,
        art.id === ual.map(_.artistId)
        )
    ).toList
  }
}

object DeezerArtistFacade extends ServiceArtistTrait {

  def apply(identifier: Either[Int,String]) = new DeezerArtistFacade(identifier)

  override protected def insertIfNotExists(id:Long):Long = {
    from(AppDB.deezerArtists)(da =>
      where(da.id === id)
        select da.id
    ).headOption match {
      case None => insert(id)
      case _ => id
    }
  }

  override protected def insert(id: Long):Long = {
    AppDB.deezerArtists.insert(DeezerArtist(id)).id
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

  override def allArtistIds: List[Long] = {
    transaction {
      from(AppDB.deezerArtists)(da => select(da.id)).toList
    }
  }
}


