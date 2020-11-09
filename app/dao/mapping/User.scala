package dao.mapping

import com.byteslounge.slickrepo.meta.Entity

sealed trait Role
case object Admin extends Role
case object Driver extends Role

case class User(override val id: Option[Long], name: String, password: String, role:Role) extends Entity[User, Long] {
  override def withId(id: Long): User = this.copy(id = Some(id))
}

