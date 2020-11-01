package dao

import dao.mapping.Entity
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.Rep
import slick.relational.RelationalProfile


trait Keyed[ID] {
  def id: Rep[ID]
}

trait BaseRepo[T <: Entity[T, ID], ID] {

  protected val dbConfigProvider: DatabaseConfigProvider
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  type TableType <: Keyed[ID] with RelationalProfile#Table[T]

  protected def tableQuery: TableQuery[TableType]

  def all() = db.run {
    tableQuery.result
  }

  def schema = tableQuery.schema

  def createTable() = db.run {
    DBIO.seq(
      schema.createIfNotExists
    )
  }
}


