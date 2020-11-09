package dao

import com.byteslounge.slickrepo.meta.Entity
import com.byteslounge.slickrepo.repository.Repository
import dao.mapping.User
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
abstract class CommonRepo[T <: Entity[T, ID], ID] @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository[T, ID](dbConfigProvider.get[JdbcProfile].profile) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  def create(item: T): Future[T] = db.run {
    save(item)
  }

  def schema = tableQuery.schema

  def createTable(): Future[Unit] = db.run {
    tableQuery.schema.createIfNotExists
  }


}
