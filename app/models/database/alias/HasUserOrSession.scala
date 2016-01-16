package models.database.alias


trait HasUserOrSession {
  def getUserId:Option[Long]
  def getUserSession:Option[String]
}
