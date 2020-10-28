package models

import play.api.libs.json.Json

case class VehicleClassO(id: Long, name: String)

case class VehicleModelO(id: Long, name: String, vehicleClass: VehicleClassO)

case class VehicleO(id: Long, name: String, licensePLate: String, model: VehicleModelO)


case class VehicleClass(id: Long, name: String)

object VehicleClass {
  implicit val classFormat = Json.format[VehicleClass]
}

case class VehicleModel(id: Long, name: String, vehicleClassId: Long)

case class Vehicle(id: Long, name: String, licensePLate: String, modelId: Long)

