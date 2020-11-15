package dao.repo

import com.byteslounge.slickrepo.meta.Keyed
import dao.mapping.{Vehicle, VehicleClass, VehicleModel}
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.BaseTypedType

import scala.concurrent.ExecutionContext

@Singleton
class VehicleRepo @Inject()(override val dbConfigProvider: DatabaseConfigProvider)
                           (implicit ec: ExecutionContext)
  extends CommonRepo[Vehicle, Long](dbConfigProvider) {

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

  val pkType = implicitly[BaseTypedType[Long]]
}

@Singleton
class VehicleClassRepo @Inject()(override val dbConfigProvider: DatabaseConfigProvider)
                                (implicit ec: ExecutionContext)
  extends CommonRepo[VehicleClass, Long](dbConfigProvider) {

  import dbConfig._
  import profile.api._

  class VehicleClasses(tag: Tag) extends Table[VehicleClass](tag: Tag, "VehicleClasses") with Keyed[Long] {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (id.?, name) <> ((VehicleClass.apply _).tupled, VehicleClass.unapply)
  }

  override type TableType = VehicleClasses
  override val tableQuery = TableQuery[TableType]
  val pkType = implicitly[BaseTypedType[Long]]


  def find(id:Long) = db.run{
    tableQuery.filter(_.id === id).result.headOption
  }
}

@Singleton
class VehicleModelRepo @Inject()(override val dbConfigProvider: DatabaseConfigProvider)
                                (implicit ex: ExecutionContext)
  extends CommonRepo[VehicleModel, Long](dbConfigProvider) {

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
  val pkType = implicitly[BaseTypedType[Long]]

}

