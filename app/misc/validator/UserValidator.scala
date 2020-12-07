package misc.validator

import scala.language.implicitConversions

object UserExtentionValidator {

  implicit def NameValidator(name: String) = new {
    def validation: ValidationResult = {
      name match {
        case name if name.isEmpty => ValidationFailed("name can't be empty")
        case name if name.charAt(0).isDigit => ValidationFailed("name can't start with number")
        case _ => Validated
      }
    }
  }

  implicit def UserValidator(user: common.User) = new {
    def validation: ValidationResult =
      user.getName.validation
  }

}
