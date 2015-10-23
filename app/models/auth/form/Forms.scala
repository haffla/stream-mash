package models.auth.form

import models.UserData
import play.api.data.Form
import play.api.data.Forms._

trait Forms {
  val usernameMaxLength = 64
  val usernameMinLength = 3
  val passwordMinLength = 6

  val registerForm = Form(
    mapping(
      "name" -> text.verifying(s"The username's length must be between $usernameMinLength and $usernameMaxLength",
        text => text.length > usernameMinLength && text.length < usernameMaxLength),
      "password" -> tuple(
        "main" -> text.verifying(s"The password must be at least $passwordMinLength characters long",
          password => password.length > passwordMinLength),
        "confirm" -> text
      ).verifying(
          "Passwords don't match", password => password._1 == password._2
        )
        .transform(
      { case (main, confirm) => main },
      (main: String) => ("", "")
      )
    )(UserData.apply)(UserData.unapply)
  )

  val loginForm = Form(
    mapping(
      "name" -> text,
      "password" -> text
    )(UserData.apply)(UserData.unapply)
  )
}
