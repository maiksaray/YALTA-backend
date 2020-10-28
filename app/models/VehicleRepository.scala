package models

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class VehicleRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class VehicleClasses(tag: Tag) extends Table[VehicleClass](tag, "VehicleClasses") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (id, name) <> ((VehicleClass.apply _).tupled, VehicleClass.unapply)
  }

  private val vehicleClasses = TableQuery[VehicleClasses]

  private class VehicleModels(tag: Tag) extends Table[VehicleModel](tag, "VehicleModels") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def vehicleClassId = column[Long]("vehicleClassId")

    def vehicleClassFk = foreignKey("vehicleClassFk", vehicleClassId, vehicleClasses)(
      _.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)

    def * = (id, name, vehicleClassId) <> ((VehicleModel.apply _).tupled, VehicleModel.unapply)
  }

  private val vehicleModels = TableQuery[VehicleModels]

  private class Vehicles(tag: Tag) extends Table[Vehicle](tag, "Vehicles") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def licensePlate = column[String]("licansePLate")

    def modelId = column[Long]("modelId")

    def modelIdFk = foreignKey("ModelFk", modelId, vehicleModels)(
      _.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)

    def * = (id, name, licensePlate, modelId) <> ((Vehicle.apply _).tupled, Vehicle.unapply)
  }

  private val vehicles = TableQuery[Vehicles]

  def insertClass(name: String) = {
    (vehicleClasses.map(p => p.name)
      // Now define it to return the id, because we want to know what id was generated for the person
      returning vehicleClasses.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((name, id) => VehicleClass(id, name))
      // And finally, insert the person into the database
      ) += (name)
  }

//  TODO: wrap this as evolution
  private val schema = vehicleModels.schema ++ vehicleClasses.schema ++ vehicles.schema

  def createDBIfNotExist(): Unit = {
    db.run(DBIO.seq(
      schema.createIfNotExists
    ))
  }

  def fillStub(): Unit = {
    createDBIfNotExist()
    val v = db.run(insertClass("D"))
  }

  def getClasses(): Future[Seq[VehicleClass]] = {
    db.run{
      vehicleClasses.result
    }
  }
}
