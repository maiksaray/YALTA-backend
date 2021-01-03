package dao

import dao.implicits.UserTransform._
import dao.mapping.User
import dao.repo.UserRepo
import javax.inject.{Inject, Singleton}
import misc.CompletionMarker
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserDao @Inject()(repo: UserRepo)(implicit ec: ExecutionContext)
  extends BaseDao[User, Long, UserRepo](repo)(ec)
    with Logging {

  override def ensureExists(): Future[CompletionMarker] =
    super.ensureExists().flatMap { _ =>
      repo.findByName("admin").flatMap {
        case None =>
          for {
            _ <- create("admin", "admin", common.Admin.INSTANCE)
            _ <- create("driver", "driver", common.Driver.INSTANCE)
          } yield CompletionMarker
        case _ => Future.successful(CompletionMarker)
      }
    }

  def create(name: String, pass: String, role: common.Role): Future[common.User] =
    create(new common.User(null, name, pass, role))

  def create(user: common.User): Future[common.User] = {
    logger.info(s"Creating user ${user.getName}")
    repo.create(user)
      .map(userDbToModel)
  }

  def update(user: common.User): Future[common.User] =
    repo.run(repo.update(user)).map(userDbToModel)

  //  TODO: maybe move this to new service layer?
  def auth(username: String, password: String): Future[Option[common.User]] = {
    logger.info(s"Authenticating user $username")
    repo.findByName(username).map {
      case Some(dbUser) if dbUser.password == password =>
        logger.info(s"User $username successfully authorized")
        Some(dbUser)
      case Some(_) =>
        logger.info(s"User $username provided incorrect password")
        None
      case None =>
        logger.info(s"User $username not found")
        None
    }
  }

  def auth(user: common.User): Future[Option[common.User]] =
    auth(user.getName, user.getPassword)

  def getUser(id: Long): Future[Option[common.User]] = {
    //    TODO:rework this!
    logger.info(s"Obtaining user with id $id")
    repo.find(id).map { option =>
      option.map { user =>
        userDbToModel(user)
      }
    }
  }

  def getUser(username: String): Future[Option[common.User]] = {
    //    TODO:rework this!
    logger.info(s"Obtaining user with name $username")
    repo.findByName(username).map { option =>
      option.map(userDbToModel)
    }
  }

}
