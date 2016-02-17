package models.database.facade.service

import models.database.AppDB
import models.database.alias._
import models.database.alias.service.NapsterArtist
import models.util.Constants
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.GroupWithMeasures
import play.api.libs.json.JsValue

class NapsterArtistFacade(identifier:Either[Int,String]) extends ServiceArtistFacade(identifier) {

  val serviceName = Constants.serviceNapster

  override def artistsAndAlbums(usersArtists:List[Long]):List[(Album,Artist,String)] = {
    join(AppDB.albums,
         AppDB.artists,
         AppDB.napsterAlbums,
         AppDB.napsterArtists,
         AppDB.userArtistLikings)( (alb,art,npAlb,npArt,ual) =>
      where(art.id in usersArtists and ual.score.gt(0) and AppDB.userWhereClause(ual,identifier))
        select(alb, art, npAlb.napsterId)
        on(
        alb.artistId === art.id,
        alb.id === npAlb.id,
        art.id === npArt.id,
        art.id === ual.artistId
        )
    ).toList
  }

}

object NapsterArtistFacade extends ServiceArtistTrait {

  override def apply(identifier: Either[Int,String]) = new NapsterArtistFacade(identifier)

  override protected def setArtistAnalysed(id: Long) = {
    update(AppDB.napsterArtists)(na =>
      where(na.id === id)
      set(na.isAnalysed := true)
    )
  }

  override protected def insertOrUpdate(id:Long):Long = {
    from(AppDB.napsterArtists)(sa =>
      where(sa.id === id)
        select sa
    ).headOption match {
      case None => insert(id)
      case Some(napsArt) =>
        if(!napsArt.isAnalysed)
          setArtistAnalysed(napsArt.id)
        napsArt.id
    }
  }

  override protected def insert(id: Long):Long = {
    AppDB.napsterArtists.insert(NapsterArtist(id, isAnalysed = true)).id
  }

  override def saveInfoAboutArtist(js: JsValue): Unit = {
    //TODO do something here
  }

  override def analysedArtistIds(artistIds: List[Long]): List[Long] = {
    transaction {
      from(AppDB.napsterArtists)(napsArt =>
        where(napsArt.isAnalysed === true and napsArt.id.in(artistIds))
        select napsArt.id
      ).toList
    }
  }

  override def artistsAlbumCount(artistId:List[Long]):List[GroupWithMeasures[Long,Long]] = {
    from(AppDB.napsterArtists, AppDB.napsterAlbums, AppDB.albums)((napsArt,napsAlb,alb) =>
      where(napsArt.id in artistId and alb.id === napsAlb.id and alb.artistId === napsArt.id)
        groupBy napsArt.id
        compute countDistinct(alb.id)
    ).toList
  }
}


