package dao.mapping

import java.sql.Timestamp

import com.byteslounge.slickrepo.meta.Entity

case class Location(override val id: Option[Long], lat: Double, lon: Double, userId: Long, timestamp: Timestamp) extends Entity[Location, Long] {
  override def withId(id: Long): Location = this.copy(id = Some(id))

  def withIdAndTimestamp(id: Long, timestamp: Timestamp): Location = this.copy(id = Some(id), timestamp = timestamp)
}
