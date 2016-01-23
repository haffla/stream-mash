package models.database.facade

import models.database.alias.{UserArtistLiking, AppDB}
import org.squeryl.PrimitiveTypeMode._

class ArtistLikingFacade(identifier:Either[Int,String]) extends Facade {

  def setScoreForArtist(artist:String, score:Double) = {
    inTransaction {
      getEntityIdByArtist(artist) match {
        case Some(id) =>
          update(AppDB.userArtistLikings)(ual =>
            where(ual.id === id)
              set(ual.score := score)
          )
        case None => createNewArtistLiking(artist, score)

      }
    }
  }

  def getEntityIdByArtist(artist:String):Option[Long] = {
    from(AppDB.userArtistLikings, AppDB.artists)((ual, a) =>
      where(a.name === artist and AppDB.userWhereClause(ual,identifier) and a.id === ual.artistId)
        select ual.id
    ).headOption
  }

  def createNewArtistLiking(artist:String, score:Double) = {
    ArtistFacade.artistByName(artist) match {
      case Some(art) =>
        val ual = identifier match {
          case Left(id) => UserArtistLiking(artistId = art.id, userId = Some(id.toLong), score = score)
          case Right(userSession) => UserArtistLiking(artistId = art.id, userSession = Some(userSession), score = score)
        }
        AppDB.userArtistLikings.insert(ual)
      case _ =>
    }
  }

  def insert(idArtist:Long, score:Double) = {
    val ual = identifier match {
      case Left(id) => UserArtistLiking(artistId = idArtist, userId = Some(id), score = score)
      case Right(userSession) => UserArtistLiking(artistId = idArtist, userSession = Some(userSession), score = score)
    }
    AppDB.userArtistLikings.insert(ual)
  }

  def insertIfNotExists(idArtist:Long, score:Double = 1) = {
    from(AppDB.userArtistLikings)(ual =>
      where(ual.artistId === idArtist and AppDB.userWhereClause(ual, identifier))
        select ual.id
    ).headOption match {
      case None => insert(idArtist, score)
      case _ =>
    }
  }
}

object ArtistLikingFacade {
  def apply(identifier:Either[Int,String]) = new ArtistLikingFacade(identifier)
}
