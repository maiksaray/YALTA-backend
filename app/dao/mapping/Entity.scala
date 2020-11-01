package dao.mapping

trait Entity[T, ID] {
  val id: Option[ID]

  def withId(id: ID): T
}