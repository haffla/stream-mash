package models.database.alias

case class Album(id: Option[Int] = None,
                 name:String,
                 artistId: Int)
