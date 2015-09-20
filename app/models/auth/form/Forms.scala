package models.auth.form

import models.UserData
import play.api.data.Form
import play.api.data.Forms._

trait Forms {
  val USERNAME_MAX_LENGTH = 64
  val USERNAME_MIN_LENGTH = 3
  val PASSWORD_MIN_LENGTH = 6

  val registerForm = Form(
    mapping(
      "name" -> text.verifying(s"The username's length must be between $USERNAME_MIN_LENGTH and $USERNAME_MAX_LENGTH",
        text => text.length > USERNAME_MIN_LENGTH && text.length < USERNAME_MAX_LENGTH),
      "password" -> tuple(
        "main" -> text.verifying(s"The password must be at least $PASSWORD_MIN_LENGTH characters long",
          password => password.length > PASSWORD_MIN_LENGTH),
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
