package dao.mapping

import java.sql.Timestamp

import com.byteslounge.slickrepo.meta.Entity

case class Location(override val id: Option[Long], lat: Double, lon: Double, userId: Long, timestamp: Timestamp) extends Entity[Location, Long] {
  override def withId(id: Long): Location = this.copy(id = Some(id))

  /**
   *  Sets new timestamp (obtained from DB upon insert) to existing Location entity
   *  @example val location = Location(None, 10.0, 10.0, 1, null)
   *           location = location.withTimestamp(new Timestamp)
   */
  def withTimestamp(timestamp: Timestamp):Location = this.copy(timestamp = timestamp)
}
