import org.scalatestplus.play.{PlaySpec, OneServerPerSuite, OneBrowserPerSuite, HtmlUnitFactory}

class AuthenticationFlowTest extends PlaySpec with OneServerPerSuite with OneBrowserPerSuite with HtmlUnitFactory {

    val home = s"http://localhost:$port"
    val user = "testuser"
    val password = "password"
    val titleHome = "Melody Mess - Index"

    "Registering, Logging in and out" must {
        "work" in {
            go to s"$home/register"
            pageTitle mustBe "Register"
            click on find(name("name")).value
            enter(user)
            click on find(name("password.main")).value
            enter(password)
            click on find(name("password.confirm")).value
            enter(password)
            submit()
            eventually {
                pageTitle mustBe titleHome
            }

            go to s"$home/logout"
            
            eventually {
                pageTitle mustBe "Login"
            }

            click on find(name("name")).value
            enter(user)
            click on find(name("password")).value
            enter(password)
            submit()
            eventually {
                pageTitle mustBe titleHome
            }
        }
    }
}