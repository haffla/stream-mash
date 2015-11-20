package models.service.library.util

import models.service.Constants
import play.api.libs.json.JsValue

trait JsonConversion {
  def convertJsonToSeq(json: Option[JsValue]):Seq[Map[String,String]] = {
    json match {
      case Some(js) =>
        doJsonConversion(js)
      case None =>
        throw new Exception(Constants.userTracksRetrievalError)
    }
  }
  def doJsonConversion(js: JsValue): Seq[Map[String, String]]
}
