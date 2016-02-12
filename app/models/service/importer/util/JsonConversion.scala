package models.service.importer.util

import models.util.Constants
import play.api.libs.json.JsValue

trait JsonConversion {
  def convertJsonToSeq(jsonList: Seq[JsValue]):Seq[Map[String,String]] = {
    if(jsonList.nonEmpty) {
      jsonList.foldLeft(Seq[Map[String,String]]()) { (prev, curr) =>
        prev ++ doJsonConversion(curr)
      }
    }
    else throw new Exception(Constants.userTracksRetrievalError)
  }
  def doJsonConversion(js: JsValue): Seq[Map[String, String]]
}
