package models.database.facade

import models.database.alias.{UserArtistLiking, AppDB}
import org.squeryl.PrimitiveTypeMode._

class ArtistLikingFacade(identifier:Either[Int,String]) extends Facade {

  def setScoreForArtist(artist:String, score:Double) = {
    transaction {
      getEntityIdByArtist(artist) match {
        case Some(id) =>
          update(AppDB.userArtistLiking)(ual =>
            where(ual.id === id)
              set(ual.score := score)
          )
        case None => createNewArtistLiking(artist, score)

      }
    }
  }

  def getEntityIdByArtist(artist:String):Option[Long] = {
    val res = identifier match {
      case Left(id) =>
        from(AppDB.userArtistLiking, AppDB.artists)((ual,a) =>
          where(a.name === artist and ual.userId === Some(id) and a.id === ual.artistId)
            select ual.id
        )
      case Right(userSession) =>
        from(AppDB.userArtistLiking, AppDB.artists)((ual,a) =>
          where(a.name === artist and ual.userSession === Some(userSession))
            select ual.id
        )
    }
    res.headOption
  }

  def createNewArtistLiking(artist:String, score:Double) = {
    ArtistFacade.getArtistByName(artist) match {
      case Some(art) =>
        val ual = identifier match {
          case Left(id) => UserArtistLiking(artistId = art.id, userId = Some(id.toLong), score = score)
          case Right(userSession) => UserArtistLiking(artistId = art.id, userSession = Some(userSession), score = score)
        }
        AppDB.userArtistLiking.insert(ual)
      case _ =>
    }
  }
}

object ArtistLikingFacade {
  def apply(identifier:Either[Int,String]) = new ArtistLikingFacade(identifier)
}
