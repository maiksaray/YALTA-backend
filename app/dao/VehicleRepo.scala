package dao

import dao.mapping.{Vehicle, VehicleClass, VehicleModel}
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

@Singleton
class VehicleRepo @Inject()(override val dbConfigProvider: DatabaseConfigProvider)
                           (implicit ec: ExecutionContext)
  extends BaseRepo[Vehicle, Long] {

  import dbConfig._
  import profile.api._

  class Vehicles(tag: Tag) extends Table[Vehicle](tag: Tag, "vehicles") with Keyed[Long] {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def plate = column[String]("plate")

    def modelId = column[Long]("modelId")

    //    def model = foreignKey("modelFK", modelId, )
    override def * = (id.?, name, plate, modelId) <> ((Vehicle.apply _).tupled, Vehicle.unapply)
  }

  override type TableType = Vehicles
  override val tableQuery = TableQuery[TableType]

  override def schema = tableQuery.schema

  def create(item: Vehicle) = db.run {
    ((tableQuery returning tableQuery.map(_.id)) += item).map(id => item.withId(id))
  }

}

@Singleton
class VehicleClassRepo @Inject()(override val dbConfigProvider: DatabaseConfigProvider)
                                (implicit ec: ExecutionContext)
  extends BaseRepo[VehicleClass, Long] {

  import dbConfig._
  import profile.api._

  class VehicleClasses(tag: Tag) extends Table[VehicleClass](tag: Tag, "VehicleClasses") with Keyed[Long] {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (id.?, name) <> ((VehicleClass.apply _).tupled, VehicleClass.unapply)
  }

  override type TableType = VehicleClasses
  override val tableQuery = TableQuery[TableType]
  override def schema = tableQuery.schema

  def create(item: VehicleClass) = db.run {
    ((tableQuery returning tableQuery.map(_.id)) += item).map(id => item.withId(id))
  }

  def find(id:Long) = db.run{
    tableQuery.filter(_.id === id).result.headOption
  }
}

@Singleton
class VehicleModelRepo @Inject()(override val dbConfigProvider: DatabaseConfigProvider)
                                (implicit ex: ExecutionContext)
  extends BaseRepo[VehicleModel, Long] {

  import dbConfig._
  import profile.api._

  class VehicleModels(tag:Tag) extends Table[VehicleModel](tag, "VehicleModels") with Keyed[Long]{
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def classId = column[Long]("classId")

    def * = (id.?, name, classId) <> ((VehicleModel.apply _).tupled, VehicleModel.unapply)
  }

  override type TableType = VehicleModels
  override val tableQuery = TableQuery[TableType]
  override def schema = tableQuery.schema

  def create(item: VehicleModel) = db.run {
    ((tableQuery returning tableQuery.map(_.id)) += item).map(id => item.withId(id))
  }
}

