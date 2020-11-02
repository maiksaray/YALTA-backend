package dao

import dao.mapping.User
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

@Singleton
class UserRepo @Inject()(override val dbConfigProvider:DatabaseConfigProvider)(implicit ec:ExecutionContext) extends BaseRepo[User,Long] {
  import dbConfig._
  import profile.api._

  class Users(tag:Tag) extends Table[User](tag, "Users") with Keyed[Long]{
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def password = column[String]("password")

    def * = (id.?, name, password) <> ((User.apply _).tupled, User.unapply)
  }

  override type TableType = Users
  override def tableQuery = TableQuery[TableType]

}
