package misc.reports

import org.joda.time.DateTime

case class PointData(name: String, finished: Boolean, ts: DateTime)

case class RouteData(name: String, driver: String, finished: Boolean, pointsData: List[PointData])
