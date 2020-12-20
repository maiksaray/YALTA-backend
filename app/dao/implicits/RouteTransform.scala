package dao.implicits

import scala.language.implicitConversions
import dao.implicits.IdTransform._
import dao.implicits.DateTimeTransform._
import dao.mapping.{Point, Route, RoutePoint}


object RouteTransform {

  def routeDbToModelWithPoints(route: Route, routePoints: java.util.List[common.RoutePoint]) = {
    new common.Route(route.id, route.driverID, route.date, routePoints, false)
  }

  implicit def routeDbToModel(route: Route): common.Route = {
    new common.Route(route.id, route.driverID, route.date, null, false)
  }

  implicit def routeModeltoDb(route: common.Route): Route = {
    Route(route.getId, route.getDriverId, route.getRouteDate)
  }

  implicit def pointDbToModel(point: Point): common.Point = {
    new common.Point(point.id, point.lat, point.lon, point.name)
  }

  implicit def pointModelToDb(point: common.Point): Point = {
    Point(point.getId, point.getLat, point.getLon, point.getName)
  }
}
