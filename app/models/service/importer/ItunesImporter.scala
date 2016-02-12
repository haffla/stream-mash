package models.service.importer

import java.io.File
import java.nio.file.Files

import scala.concurrent.Future
import scala.xml.Node
import scala.concurrent.ExecutionContext.Implicits.global

class ItunesImporter(identifier: Either[Int, String], persist:Boolean = true)
                                    extends Importer(identifier, "itunes", persist) {

  val labelDict = "dict"
  val labelKey  = "key"
  val informationToExtract = List("Artist", "Album", "Name")
  val minTupleLength = informationToExtract.length

  /**
   * Parses the Itunes Library XML file and returns all songs
   * as a sequence of maps
   * See https://bcomposes.wordpress.com/2012/05/04/basic-xml-processing-with-scala/
   */
  private def parseXml(xmlPath: String):Seq[Map[String,String]] = {
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

  def processAndSave(xmlPath: String):Future[Map[String, Map[String,Set[String]]]] = {
    apiHelper.setRetrievalProcessPending()
    Future {
      val lib:Seq[Map[String,String]] = parseXml(xmlPath)
      val seq = convertSeqToMap(lib, informationToExtract.head, informationToExtract(1), informationToExtract(2))
      cleanUp(xmlPath)
      seq
    }
  }

  private def cleanUp(xmlPath: String) = {
    val f = new File(xmlPath)
    Files.delete(f.toPath)
  }
}
