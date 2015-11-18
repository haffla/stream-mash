package models.database.alias

case class Album(id: Option[Int] = None, name:String, interpret: String, fkUser: Int, userSessionKey: Option[String] = None)
