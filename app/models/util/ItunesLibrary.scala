package models.util

import scala.xml.Node

class ItunesLibrary(pathToXmlFile: String) {
  val LABEL_DICT = "dict"
  val LABEL_KEY  = "key"
  val information = List("Artist", "Album", "Name")

  /**
   * parses the Itunes Library XML file and returns all songs
   * as a sequence of maps
   */
  def parseXml():Seq[Map[String,String]] = {
    val xml = scala.xml.XML.loadFile(pathToXmlFile)
    val dict = xml \ LABEL_DICT \ LABEL_DICT \ LABEL_DICT
    val library = dict.map { d =>
      val keys = (d \ LABEL_KEY).toList
      val other = (d \ "_").toList.filter(x => x.label != LABEL_KEY)
      val zp:List[(Node,Node)] = keys.zip(other)
      zp.filter(information contains _._1.text)
        .map(x => (x._1.text,x._2.text)).toMap
    }
    library
  }
}
