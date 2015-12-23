import org.scalatest.selenium.HtmlUnit
import org.scalatest.{FlatSpec, Matchers}

class RegistrationTest extends FlatSpec with Matchers with HtmlUnit {

  ignore should "work" in {
      go to "http://localhost:9000/register"
      pageTitle should be ("Register")
      click on name("name")
      enter("tester")
      click on name("password.main")
      enter("password")
      click on name("password.confirm")
      enter("password")
      submit()
      pageTitle should be ("Melody Mess - Index")
    }
}
