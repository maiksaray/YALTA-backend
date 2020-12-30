package services

import com.google.inject.{Inject, Singleton}
import common.User
import dao.UserDao
import exceptions.{InvalidDataException, YaltaBaseException}
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}
import misc.validator.UserExtentionValidator._
import misc.validator.{Validated, ValidationFailed}

@Singleton
class UserService @Inject()(userDao: UserDao)(implicit ec: ExecutionContext) extends Logging {

  def get(id: Long): Future[Option[User]] = userDao.getUser(id)

  def get(name: String): Future[Option[User]] = userDao.getUser(name)

  def createUser(user: common.User): Future[common.User] =
    user.validation match {
      case Validated => userDao.create(user)
      case ValidationFailed(reason) => throw new InvalidDataException(reason)
    }

  def createUser(name: String, pass: String, role: common.Role): Future[common.User] =
    createUser(new common.User(null, name, pass, role))

  def update(name: String, change: common.User => common.User): Future[User] =
    userDao.getUser(name).map {
      case None => throw new YaltaBaseException(s"User $name does not exist, can't update")
      case Some(user) => user
    }.flatMap {
      user => userDao.update(change(user))
    }

  def changeRole(name: String, newRole: common.Role): Future[common.User] =
    update(name, u => new common.User(u.getId, u.getName, u.getPassword, newRole))

  def changePass(name: String, newPass: String): Future[User] =
    update(name, u => new common.User(u.getId, u.getName, newPass, u.getRole))
}
