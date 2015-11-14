package database.alias

case class Artist(id: Option[Int] = None, name:String, spotifyId:Option[String] = None, rdioId:Option[String] = None)
