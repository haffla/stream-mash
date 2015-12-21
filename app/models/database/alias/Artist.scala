package models.database.alias

case class Artist(id: Option[Int] = None,
                  name:String,
                  spotifyId:Option[String] = None,
                  rdioId:Option[String] = None,
                  soundcloudId:Option[String] = None,
                  deezerId:Option[String] = None,
                  lastfmId:Option[String] = None)
