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

  def auth(username: String, password: String) = {
    repo.findByName(username).map {
      case Some(dbUser) => dbUser.password == password
      case None => false
    }
  }

  def auth(user: common.User) = {
    repo.findByName(user.getName).map {
      case Some(dbUser) => dbUser.password == user.getPassword
      case None => false
    }
  }

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
