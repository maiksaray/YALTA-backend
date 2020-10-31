package dao

import dao.mapping.Vehicle
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

@Singleton
class VehicleRepository @Inject()(override val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends BaseRepo [Vehicle, Long]{

  import dbConfig._
  import profile.api._

  type TableType = Vehicles
  val tableQuery = TableQuery[TableType]

  class Vehicles(tag:Tag) extends Table[Vehicle](tag:Tag, "vehicles") with Keyed[Long] {
     def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def plate = column[String]("plate")
    def modelId = column[Long]("modelId")

//    def model = foreignKey("modelFK", modelId, )
    override def * = (id.?, name,plate,modelId) <> ((Vehicle.apply _).tupled, Vehicle.unapply)
  }

  def create(vehicle: Vehicle) = db.run{
    ((tableQuery returning tableQuery.map(_.id)) += vehicle).map(id => vehicle.withId(id))
  }

  override def schema = tableQuery.schema


}
