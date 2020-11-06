package dao.implicits

import IdTransform._
import dao.mapping.{Admin, Driver, Role, User}
import scala.language.implicitConversions

object UserTransform {

  implicit def roleDbToModel(role: Role): common.Role = role match {
    case Admin => common.Admin.INSTANCE
    case Driver => common.Driver.INSTANCE
  }

  implicit def roleModeltoDb(role: common.Role): Role = role match {
    case common.Admin.INSTANCE => Admin
    case common.Driver.INSTANCE => Driver
  }

  implicit def userDbToModel(user: User): common.User = {
    new common.User(user.id, user.name, user.password, user.role)
  }

  implicit def userModelToDb(user: common.User): User = {
    User(user.getId, user.getName, user.getPassword, user.getRole)
  }
}
