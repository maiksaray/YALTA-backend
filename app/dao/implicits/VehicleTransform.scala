package dao.implicits

import dao.mapping.{Vehicle, VehicleClass}
import scala.language.implicitConversions

object VehicleTransform {

  implicit def nullableToOption(id: java.lang.Long): Option[Long] = id match {
    case null => Some(id)
    case _ => None
  }

  implicit def OptionToNullableLong(id:Option[Long]) :java.lang.Long = id match {
    case Some(v) => v
    case None => null
  }

  implicit def vehicleClassDbToModes(vehicleClass: VehicleClass): common.VehicleClass =
//    TODO: universally pass unapplied case class to tupled constructor???
    new common.VehicleClass(vehicleClass.id, vehicleClass.name)

  implicit def vehicleClassModelToDb(vehicleClass: common.VehicleClass): VehicleClass =
    VehicleClass(vehicleClass.getId, vehicleClass.getName)

}
