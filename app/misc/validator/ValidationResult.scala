package misc.validator

sealed trait ValidationResult

case object Validated extends ValidationResult

abstract class ValidationFailed(reason: String) extends ValidationResult
