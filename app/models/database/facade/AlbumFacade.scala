package models.database.facade

import models.database.alias.{Album, AppDB}
import org.squeryl.PrimitiveTypeMode._

object AlbumFacade {
  def apply(identifier:Either[Int,String]) = new AlbumFacade(identifier)
}

class AlbumFacade(identifier:Either[Int,String]) extends Facade {

  def getUsersFavouriteAlbums:List[Album] = {
    transaction {
      join(
        AppDB.albums,
        AppDB.tracks,
        AppDB.collections,
        AppDB.userArtistLikings.leftOuter)((alb,tr,col,ual) =>
        where((ual.map(_.score).isNull or ual.map(_.score).gt(0)) and AppDB.userWhereClause(col,identifier))
          select alb
          on(
          alb.id === tr.albumId,
          col.trackId === tr.id,
          ual.map(_.artistId) === alb.artistId and AppDB.joinedAndOuterJoinedEntitiesHaveMatchingUserRelation(col,ual,identifier)
          )
      ).toList
    }
  }
}
