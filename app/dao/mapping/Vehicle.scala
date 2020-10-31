package dao.mapping

case class Vehicle(override val id: Option[Long], name: String, plate: String, modelId: Long) extends Entity[Vehicle, Long] {
  override def withId(id: Long): Vehicle = this.copy(id = Some(id))
}
