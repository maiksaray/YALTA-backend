package dao.mapping

import com.byteslounge.slickrepo.meta.Entity

case class VehicleClass(override val id: Option[Long], name: String) extends Entity[VehicleClass, Long] {
  override def withId(id: Long): VehicleClass = this.copy(id = Some(id))
}

case class VehicleModel(override val id: Option[Long], name: String, classId: Long) extends Entity[VehicleModel, Long] {
  override def withId(id: Long): VehicleModel = this.copy(id = Some(id))
}

case class Vehicle(override val id: Option[Long], name: String, plate: String, modelId: Long) extends Entity[Vehicle, Long] {
  override def withId(id: Long): Vehicle = this.copy(id = Some(id))
}
