package models

case class Account(id:Option[Int] = None, name:String, password:String, itunesFileHash:Option[String] = None)
