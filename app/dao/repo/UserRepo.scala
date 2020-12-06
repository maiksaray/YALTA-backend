package dao.repo

import com.byteslounge.slickrepo.meta.Keyed
import dao.mapping.{Admin, Driver, Role, User}
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.BaseTypedType

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepo @Inject()(override val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends CommonRepo[User, Long](dbConfigProvider) {

  import dbConfig._
  import profile.api._

  implicit def roleMapper = MappedColumnType.base[Role, Int](
    {
      case Admin => 1
      case Driver => 2
    },
    {
      case 1 => Admin
      case 2 => Driver
    }
  )

  class Users(tag: Tag) extends Table[User](tag, "users") with Keyed[Long] {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def password = column[String]("password")

    def role = column[Role]("role")

    def * = (id.?, name, password, role) <> ((User.apply _).tupled, User.unapply)
  }

  type TableType = Users
  val pkType = implicitly[BaseTypedType[Long]]
  val tableQuery = TableQuery[TableType]


  def find(id: Long): Future[Option[User]] = db.run {
    tableQuery.filter(_.id === id).result.headOption
  }

  def findByName(name: String): Future[Option[User]] = db.run {
    tableQuery.filter(_.name === name).result.headOption
  }
}
