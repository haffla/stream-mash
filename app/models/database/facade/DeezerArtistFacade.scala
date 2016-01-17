package models.database.facade

import models.database.alias._
import org.squeryl.PrimitiveTypeMode._

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

  def apply(identifier: Either[Int,String]) = new SpotifyArtistFacade(identifier)

  override def insertArtist(id:Long):Long = {
    from(AppDB.deezerArtists)(da =>
      where(da.id === id)
        select da.id
    ).headOption match {
      case None => AppDB.spotifyArtists.insert(SpotifyArtist(id)).id
      case _ => id
    }
  }
}


