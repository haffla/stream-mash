import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

abstract class UnitSpec extends FlatSpec with Matchers with
  OptionValues with Inside with Inspectors with ScalaFutures