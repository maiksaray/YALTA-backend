package dao.mapping

trait Entity[T <: Entity[T, ID], ID] {
  val id: Option[ID]

  def withId(id: ID): T
}
