package models.database.facade.service

import org.squeryl.PrimitiveTypeMode._
import play.api.libs.json.JsValue

trait ServiceArtistTrait {

  protected def insertIfNotExists(id:Long):Long
  protected def insert(id:Long):Long
  def saveInfoAboutArtist(js:JsValue):Unit
  def allArtistIds:List[Long]
  def saveArtist(id:Long):Long = inTransaction(insertIfNotExists(id))

}
