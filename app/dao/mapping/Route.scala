package dao.mapping

import java.sql.Date
import java.time.Instant

import com.byteslounge.slickrepo.meta.Entity

case class Point(override val id: Option[Long], lat: Double, lon: Double, name: String) extends Entity[Point, Long] {
  override def withId(id: Long): Point = this.copy(id = Some(id))
}

case class RoutePoint(override val id: Option[Long], routeId: Long, pointId: Long, visited: Boolean, index: Int, updated: Instant) extends Entity[RoutePoint, Long] {
  override def withId(id: Long): RoutePoint = this.copy(id = Some(id))
}

case class Route(override val id: Option[Long], driverID: Option[Long], date: Date) extends Entity[Route, Long] {
  override def withId(id: Long): Route = this.copy(id = Some(id))
}

