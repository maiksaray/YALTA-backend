package misc

class YaltaBaseException(val message: String) extends Exception(message)

class UnknownYaltaException(override val message: String) extends YaltaBaseException(message)


class InvalidDataException(override val message: String) extends YaltaBaseException(message)

class InvalidUsernameException(override val message: String) extends InvalidDataException(message)

class NotExistException(override val message: String) extends YaltaBaseException(message)

class DuplicateRouteException(override val message: String) extends YaltaBaseException(message)

class UpdateException(override val message: String) extends YaltaBaseException(message)
