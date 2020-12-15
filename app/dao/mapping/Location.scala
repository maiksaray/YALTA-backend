package dao.mapping

import java.time.Instant

import com.byteslounge.slickrepo.meta.Entity
import org.joda.time.DateTime

case class Location(override val id: Option[Long], lat: Double, lon: Double, userId: Long, timestamp: Instant) extends Entity[Location, Long] {
  override def withId(id: Long): Location = this.copy(id = Some(id))

  /**
   *  Sets new timestamp (obtained from DB upon insert) to existing Location entity
   *  @example val location = Location(None, 10.0, 10.0, 1, null)
   *           location = location.withTimestamp(new Timestamp)
   */
  def withTimestamp(timestamp: Instant):Location = this.copy(timestamp = timestamp)
}
