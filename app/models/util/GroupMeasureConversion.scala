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

  def mergeMaps(listOfMaps:List[Map[Long,Long]]):Map[Long,Long] = {
    (Map[Long,Long]() /: (for (m <- listOfMaps; kv <- m) yield kv)) { (a, kv) =>
      a + ( if(a.contains(kv._1)) kv._1 -> (a(kv._1) + kv._2) else kv)
    }
  }
}
