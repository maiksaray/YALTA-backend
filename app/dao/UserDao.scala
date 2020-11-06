package dao

import dao.mapping.{User}
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext
import implicits.UserTransform._

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

  def auth(user: common.User) = {
    repo.findByName(user.getName).map {
      case Some(dbUser) => dbUser.password == user.getPassword
      case None => false
    }
  }

}
