package models.database.facade

import models.database.alias._
import models.database.alias.service.DeezerArtist
import org.squeryl.PrimitiveTypeMode._
import play.api.libs.json.JsValue

class DeezerArtistFacade(identifier:Either[Int,String]) extends ServiceArtistFacade(identifier) {

  val serviceName = "deezer"

  override def joinWithArtistsAndAlbums(usersArtists:List[Long]):List[(Album,Artist,String)] = {
    join(AppDB.albums,
         AppDB.artists,
         AppDB.deezerAlbums,
         AppDB.deezerArtists)( (alb,art,deeAlb,deeArt) =>
      where(art.id in usersArtists)
        select(alb, art, deeAlb.deezerId)
        on(
        alb.artistId === art.id,
        alb.id === deeAlb.id,
        art.id === deeArt.id
        )
    ).toList
  }

}

object DeezerArtistFacade extends ServiceArtistTrait {

  def apply(identifier: Either[Int,String]) = new DeezerArtistFacade(identifier)

  override def insertIfNotExists(id:Long):Long = {
    from(AppDB.deezerArtists)(da =>
      where(da.id === id)
        select da.id
    ).headOption match {
      case None => insert(id)
      case _ => id
    }
  }

  override def insert(id: Long):Long = {
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
}


