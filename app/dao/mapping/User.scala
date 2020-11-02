package dao.mapping

case class User(override val id: Option[Long], name: String, password: String) extends Entity[User, Long] {
  override def withId(id: Long): User = this.copy(id = Some(id))
}

