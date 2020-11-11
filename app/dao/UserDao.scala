package dao

import dao.mapping.User
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import implicits.UserTransform._
import sun.security.util.Password

@Singleton
class UserDao @Inject()(repo: UserRepo)(implicit ec: ExecutionContext) {

  //  TODO:Move this to a special service
  def ensureExists() =
    repo.createTable()

  def create(name: String, pass: String, role: common.Role) = {
    repo.create(User(None, name, pass, role))
  }

  def create(user: common.User) = {
    repo.create(user)
  }

  def auth(username: String, password: String): Future[Option[common.User]] = {
    repo.findByName(username).map {
      case Some(dbUser) if dbUser.password == password =>
        Some(dbUser)
      case None => None
    }
  }

  def auth(user: common.User): Future[Option[common.User]] =
    auth(user.getName, user.getPassword)

  def getUser(username: String): Future[Option[common.User]] = {
    //    TODO:rework this!
    repo.findByName(username).map {
      f =>
        f.map { u =>
          userDbToModel(u)
        }
    }
  }

}
