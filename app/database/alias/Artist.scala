package database.alias
import scalikejdbc._

case class Artist(id: Option[Int] = None, name:String, spotifyId:Option[String] = None, rdioId:Option[String] = None)

// Used by ScalikeJDBC
object Artist extends SQLSyntaxSupport[Artist] {
  override val tableName = "artist"
  def apply(rs:WrappedResultSet) = new Artist(
    rs.intOpt("id_artist"),
    rs.string("name"),
    rs.stringOpt("spotify_id"),
    rs.stringOpt("rdio_id")
  )
}
