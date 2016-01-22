package models.database.alias

import org.squeryl.dsl.ast.LogicalBoolean
import org.squeryl.PrimitiveTypeMode._

trait Conditionals {
  def userWhereClause(userRelatedEntity:HasUserOrSession, id:Either[Int,String]):LogicalBoolean = {
    id match {
      case Left(i) => userRelatedEntity.getUserId === i
      case Right(userSession) => userRelatedEntity.getUserSession === Some(userSession)
    }
  }

  /**
    * If the entity exists then either userIds or userSessions must equal
    */
  def existsAndBelongsToUser(outerJoinableEntity: Option[HasUserOrSession], identifier: Either[Int, String]): LogicalBoolean = {
    outerJoinableEntity match {
      case Some(entity) =>
        identifier match {
          case Left(id) => entity.getUserId === id
          case Right(session) => entity.getUserSession === Some(session)
        }
      case _ => 1 === 0
    }
  }

  /**
    * Return true if it does not exist
    */
  def doesNotExist(outerJoinedEntity: Option[OuterJoinedArtistRelatedEntity]): LogicalBoolean = {
    outerJoinedEntity match {
      case Some(entity) => entity.getArtistId.isNull
      case _ => 1 === 1
    }
  }

  def joinedAndOuterJoinedEntitiesHaveMatchingUserRelation(joinedEntity: HasUserOrSession, outerJoinedEntity: Option[HasUserOrSession], identifier: Either[Int, String]): LogicalBoolean = {
    identifier match {
      case Left(_) => joinedEntity.getUserId === outerJoinedEntity.map(_.getUserId).getOrElse(None)
      case Right(_) =>
        val userSessionOne:Option[String] = joinedEntity.getUserSession
        val userSessionTwo:Option[String] = outerJoinedEntity.map(_.getUserSession).getOrElse(None)
        userSessionOne === userSessionTwo
    }
  }

  def outerJoinedEntityBelongsToUser(outerJoinedEntity: Option[HasUserOrSession], identifier: Either[Int, String]): LogicalBoolean = {
    identifier match {
      case Left(userId) =>
        Some(userId) === outerJoinedEntity.map(_.getUserId).getOrElse(None)
      case Right(userSession) =>
        val userSessionOne:Option[String] = outerJoinedEntity.map(_.getUserSession).getOrElse(None)
        val userSessionTwo:Option[String] = Some(userSession)
        userSessionOne === userSessionTwo
    }
  }
}
