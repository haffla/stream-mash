package models.database.facade.service

trait ServiceAlbumTrait {
  def countMissingUserAlbums(artistIds:List[Long]): Long
}
