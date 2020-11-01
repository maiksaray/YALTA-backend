package dao

import dao.mapping.VehicleClass
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import implicits.VehicleTransform._

@Singleton
class VehicleDao @Inject()(vehicleRepo: VehicleRepo, vehicleClassRepo: VehicleClassRepo, vehicleModelRepo: VehicleModelRepo)
                          (implicit ec: ExecutionContext) {

  def ensureExists() =
    for {
      _ <- vehicleRepo.createTable()
      _ <- vehicleModelRepo.createTable()
      _ <- vehicleClassRepo.createTable()
    } yield {}

  def createClass(name: String): Future[common.VehicleClass] =
//    NO IDEA WHY IMPLICIT DOES NOT WORK HERE
    vehicleClassRepo.create(VehicleClass(None, name)).map(vehicleClassDbToModes)

  //  def getVehicle(id: Long): Vehicle = {
  //    new Vehicle(0L, "a", "ab", new VehicleModel(0L, "as", new VehicleClass(0L, "a")))
  //  }

  def getVehicleClass(id: Long): Future[Option[common.VehicleClass]] =
    vehicleClassRepo.find(id)
      .map {
        case Some(vehicleClass) => Some(vehicleClass)
        case None => None
      }
}
