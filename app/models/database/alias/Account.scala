package models.database.alias

case class Account(id:Option[Int] = None,
                   name:String, password:String,
                   itunesFileHash:Option[String] = None,
                   spotifyToken:Option[String] = None,
                   rdioToken:Option[String] = None,
                   deezerToken:Option[String] = None,
                   soundcloudToken:Option[String] = None,
                   lastfmToken:Option[String] = None)
