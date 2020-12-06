package misc.validator

sealed trait ValidationResult

case object Validated extends ValidationResult

case class ValidationFailed(reason: String) extends ValidationResult
