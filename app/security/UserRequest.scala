package security

import common.User
import play.api.mvc.{Request, WrappedRequest}

import scala.concurrent.Future

class UserRequest[A](val user: Future[Option[User]], request: Request[A])
  extends WrappedRequest[A](request)
