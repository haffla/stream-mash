package models.database.alias

import org.squeryl.Schema

object AppDB extends Schema {
  val userTable = table[User]("account")
}