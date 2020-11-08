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

    val un = req.session.get("sessionToken")
      .flatMap(sessionDao.getSession)
      .filter(_.expiration.isAfter(LocalDateTime.now()))
      .map(_.username)
    //      .map(userDao.getUser)
    // TODO: come up with more idiomatic way to propagate this Option and not get Option[Future[Option...]]
    un match {
      case None => Future(None)
      case Some(value) => userDao.getUser(value)
    }
  }

  def transform[A](request: Request[A]): Future[UserRequest[A]] = Future.successful {
    val user = extractUser(request)
    new UserRequest(user, request)
  }
}
