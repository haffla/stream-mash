package models.database.facade

import scalikejdbc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CollectionFacade {
  def apply(identifier:Either[Int,String]) = new CollectionFacade(identifier)
}

class CollectionFacade(identifier:Either[Int,String]) extends Facade {
  def userCollection:Future[List[Map[String, Any]]] = {
    Future {
      val userId = identifier match {
        case Left(id) => id
        case Right(userSession) => userSession
      }
      val userClause:SQLSyntax = identifier match {
        case Left(_) => sqls"uc.fk_user = $userId"
        case Right(_) => sqls"uc.user_session = $userId"
      }
      sql"select * from user_collection uc join track t on (uc.fk_track = t.id_track) join album alb on (t.fk_album = alb.id_album) join artist art on (art.id_artist = alb.fk_artist) where $userClause"
        .toMap().list().apply()
    }
  }
}