package dao.implicits

import dao.mapping.{Vehicle, VehicleClass}
import scala.language.implicitConversions

import IdTransform._

object VehicleTransform {


  implicit def vehicleClassDbToModes(vehicleClass: VehicleClass): common.VehicleClass =
//    TODO: universally pass unapplied case class to tupled constructor???
    new common.VehicleClass(vehicleClass.id, vehicleClass.name)

  implicit def vehicleClassModelToDb(vehicleClass: common.VehicleClass): VehicleClass =
    VehicleClass(vehicleClass.getId, vehicleClass.getName)

}
