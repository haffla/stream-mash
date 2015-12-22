import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.selenium.HtmlUnit

class RegistrationTest extends FlatSpec with Matchers with HtmlUnit {

  "Test" should "work" in {
      go to "http://localhost:9000/register"
      pageTitle should be ("Register")
      click on name("name")
      enter("tester")
      click on name("password.main")
      enter("password")
      click on name("password.confirm")
      enter("password")
      submit()
    }
}
