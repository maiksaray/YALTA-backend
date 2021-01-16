package security

import java.time.LocalDateTime

import common.{Role, User}
import dao.{SessionDao, UserDao}
import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class UserAction @Inject()(val parser: BodyParsers.Default,
                           sessionDao: SessionDao,
                           userDao: UserDao)
                          (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent]
    with ActionTransformer[Request, UserRequest] {


  private def extractUser(req: RequestHeader): Future[Option[User]] = {
    req.session.get("sessionToken") match {
      case Some(token) => sessionDao.getSession(token).flatMap {
        case Some(session) if session.expiration.isAfter(LocalDateTime.now()) =>
          userDao.getUser(session.username)
        case None => Future.successful(None)
      }
      case None => Future.successful(None)
    }
  }

  def transform[A](request: Request[A]): Future[UserRequest[A]] =
    Future.successful(
      new UserRequest(extractUser(request), request)
    )
}
