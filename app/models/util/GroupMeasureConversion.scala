package models.util

import org.squeryl.dsl.GroupWithMeasures
import play.api.libs.json.{Json, JsValue}

trait GroupMeasureConversion {

  def toMap[A](groupMeasure:List[GroupWithMeasures[A,A]]):Map[A,A] = {
    groupMeasure.foldLeft(Map[A,A]()) { (prev,curr) =>
      prev + (curr.key -> curr.measures)
    }
  }

  def toJson(map:Map[Long,Long]):JsValue = {
    val stringMap = map.foldLeft(Map[String, Long]()) { (prev, curr) =>
      prev + (curr._1.toString -> curr._2)
    }
    Json.toJson(stringMap)
  }
}
