package misc.validator

import scala.language.implicitConversions

case class NameValidationFailed(reason: String) extends ValidationFailed(reason)

object UserExtentionValidator {

  implicit def NameValidator(name: String) = new {
    def validation: ValidationResult =
      name match {
        case name if name.isEmpty => NameValidationFailed("name can't be empty")
        case name if name.charAt(0).isDigit => NameValidationFailed("name can't start with number")
        case _ => Validated
      }
  }

  implicit def UserValidator(user: common.User) = new {
    def validation: ValidationResult =
      user.getName.validation
  }

}
