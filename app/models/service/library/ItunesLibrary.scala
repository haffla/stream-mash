package models.service.library

import scala.xml.Node

class ItunesLibrary(identifier: Either[Int, String], xmlPath:String = "")
                                    extends Library(identifier, "itunes") {

  val labelDict = "dict"
  val labelKey  = "key"
  val informationToExtract = List("Artist", "Album")
  val minTupleLength = informationToExtract.length

  /**
   * Parses the Itunes Library XML file and returns all songs
   * as a sequence of maps
   */
  private def parseXml:Seq[Map[String,String]] = {
    val xml = scala.xml.XML.loadFile(xmlPath)
    val dict = xml \ labelDict \ labelDict \ labelDict
    val totalLength = dict.length
    dict.zipWithIndex.map { case (d,i) =>
      val position = i + 1
      apiHelper.setRetrievalProcessProgress(position.toDouble / totalLength / 3)
      val keys = (d \ labelKey).toList
      val other = (d \ "_").toList.filter(x => x.label != labelKey)
      val zp:List[(Node,Node)] = keys.zip(other)
      zp.filter(informationToExtract contains _._1.text)
        .map {
        x => (x._1.text,x._2.text)
      }.toMap
    }.filter(_.size >= minTupleLength)
  }

  def saveCollection():Unit = {
    apiHelper.setRetrievalProcessPending()
    val lib:Seq[Map[String,String]] = parseXml
    convertSeqToMap(lib, informationToExtract.head, informationToExtract(1))
    apiHelper.setRetrievalProcessDone()
  }
}
