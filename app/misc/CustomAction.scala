package misc

import java.time.LocalDateTime

import common.User
import javax.inject.Inject
import play.api.mvc.{ActionBuilder, ActionTransformer, AnyContent, BodyParsers, Request, RequestHeader, WrappedRequest}

import scala.concurrent.{ExecutionContext, Future}

class UserRequest[A](val user: Option[User], request: Request[A])
  extends WrappedRequest[A](request)
//
//class UserAction @Inject()
//(val parser: BodyParsers.Default)
//(implicit val executionContext: ExecutionContext)
//  extends ActionBuilder[UserRequest, AnyContent]
//    with ActionTransformer[Request, UserRequest] {
//
//  private def extractUser(req: RequestHeader): Option[User] = {
//
//    val sessionTokenOpt = req.session.get("sessionToken")
//
//    sessionTokenOpt
//      .flatMap(token => SessionDAO.getSession(token))
//      .filter(_.expiration.isAfter(LocalDateTime.now()))
//      .map(_.username)
//      .flatMap(UserDAO.getUser)
//  }
//
//  def transform[A](request: Request[A]) = Future.successful {
//    val user = extractUser(request)
//    new UserRequest(user, request)
//  }
//}
