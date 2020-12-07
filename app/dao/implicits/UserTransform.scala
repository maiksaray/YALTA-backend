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
//      Can't pattern-match here since deserialized kotlin objects are never the same
//      fromJson(toJson(common.Driver.INSTANCE), classOf[common.Role]) != common.Driver.INSTANCE
    case _ if role.isInstanceOf[common.Admin] => Admin
    case _ if role.isInstanceOf[common.Driver] => Driver
  }

  implicit def userDbToModel(user: User): common.User = {
    new common.User(user.id, user.name, user.password, user.role)
  }

  implicit def userModelToDb(user: common.User): User = {
    User(user.getId, user.getName, user.getPassword, user.getRole)
  }
}
