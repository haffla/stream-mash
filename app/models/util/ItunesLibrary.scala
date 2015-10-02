package models.util

import scala.xml.Node

class ItunesLibrary(pathToXmlFile: String) {
  val LABEL_DICT = "dict"
  val LABEL_KEY  = "key"
  val information = List("Artist", "Album")

  /**
   * parses the Itunes Library XML file and returns all songs
   * as a sequence of maps
   */
  def parseXml():Seq[Map[String,String]] = {
    val xml = scala.xml.XML.loadFile(pathToXmlFile)
    val dict = xml \ LABEL_DICT \ LABEL_DICT \ LABEL_DICT
    val min_length = information.length
    dict.map { d =>
      val keys = (d \ LABEL_KEY).toList
      val other = (d \ "_").toList.filter(x => x.label != LABEL_KEY)
      val zp:List[(Node,Node)] = keys.zip(other)
      zp.filter(information contains _._1.text)
        .map(x => (x._1.text,x._2.text)).toMap
    }.filter(_.size >= min_length)
  }

  def getLibrary(lib:Seq[Map[String,String]]): Map[String, Set[String]] = {
    lib.foldLeft(Map[String, Set[String]]()) {(prev, curr) =>
      val artist:String = curr("Artist")
      val album:String = curr("Album")
      val artistAlbums:Set[String] = prev get artist match {
        case None => Set.empty
        case Some(albums) => albums
      }
      val added:Set[String] = artistAlbums + album
      prev + (artist -> added)
    }
  }
}