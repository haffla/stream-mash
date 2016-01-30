package models.database.facade.service

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.GroupWithMeasures
import play.api.libs.json.JsValue

trait ServiceArtistTrait {

  protected def insertOrUpdate(id:Long):Long
  protected def insert(id:Long):Long
  protected def setArtistAnalysed(id: Long)
  def saveInfoAboutArtist(js:JsValue):Unit
  def analysedArtistIds(artistIds: List[Long]):List[Long]
  def saveArtist(id:Long):Long = inTransaction(insertOrUpdate(id))
  def artistsAlbumCount(artistId:List[Long]):List[GroupWithMeasures[Long,Long]]

  def countArtistsAlbums(artistId:List[Long]) = {
    inTransaction {
      artistsAlbumCount(artistId)
    }
  }
}
