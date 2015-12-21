package models.database.alias

case class Track(id: Option[Int] = None,
                  name:String,
                  artistId:Int,
                  albumId:Option[Int] = None)
