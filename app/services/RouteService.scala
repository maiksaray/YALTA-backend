package services

import java.sql.Timestamp
import java.{lang, util}

import com.google.inject.{Inject, Singleton}
import common.{Route, RoutePoint}
import dao.RouteDao
import exceptions.YaltaBaseException
import org.joda.time.DateTime
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RouteService @Inject()(routeDao: RouteDao)(implicit ec: ExecutionContext) extends Logging {


  def getPoints(): Future[List[common.Point]] =
    routeDao.getPoints().map {
      seq => seq.toList
    }

  def getPoint(pointId: Long): Future[Option[common.Point]] =
    routeDao.getPoint(pointId)

  def updatePointState(routeId: Long, routePointId: Long, userId: Long): Future[common.RoutePoint] = ???

  def assignRoute(routeId: Long, driverId: Long): Future[Route] = ???

  def getCurrentRoute(userId: Long): Future[common.Route] = ???

  // point part
  def createPoint(lat: Double, lon: Double, name: String): Future[common.Point] =
    createPoint(new common.Point(null, lat, lon, name))

  def createPoint(point: common.Point): Future[common.Point] = {
    routeDao.createPoint(point)
  }

  def updatePoint(id: Long, change: common.Point => common.Point): Future[common.Point] = {
    routeDao.getPoint(id).map {
      case None => throw new YaltaBaseException(s"Point with $id does not exist, can't update")
      case Some(point) => point
    }.flatMap {
      point => routeDao.updatePoint(change(point))
    }
  }

  def changePointName(id: Long, name: String): Future[common.Point] = {
    updatePoint(id, point => new common.Point(point.getId, point.getLat, point.getLon, name))
  }

  def changePointLocation(pointId: Long, lat: Double, lon: Double): Future[common.Point] =
    updatePoint(pointId, point => new common.Point(point.getId, lat, lon, point.getName))

  //
  //  // route point part
  //  def createRoutePoint(point: common.Point): Future[common.RoutePoint] =
  //    createRoutePoint(new common.RoutePoint(null, point, false))

  //  def updateRoutePoint(id: Long, change: RoutePoint => RoutePoint): Future[RoutePoint] = {
  //    routeDao.getRoutePoint(id).map {
  //      case None => throw new YaltaBaseException(s"RoutePoint with $id does not exist, can't update")
  //      case Some(routePoint) => routePoint
  //    }.flatMap {
  //      routePoint => routeDao.updateRoutePoint(change(routePoint))
  //    }
  //  }
  //
  //  def changeRoutePointStatus(id: Long, visited: Boolean): Future[RoutePoint] = {
  //    updateRoutePoint(id, routePoint => new RoutePoint(routePoint.getId, routePoint.getPoint, visited))
  //  }


  // route part
  def createRoute(routeDate: DateTime, points: util.ArrayList[RoutePoint]): Future[Route] =
    createRoute(new Route(null, null, routeDate, points, false))

  def createRoute(driverId: Long, routeDate: DateTime, points: util.ArrayList[RoutePoint]): Future[Route] =
    createRoute(new Route(null, driverId, routeDate, points, false))

  def createRoute(route: common.Route): Future[Route] = {
    routeDao.createRoute(route)
  }

  def updateRoute(id: Long, change: Route => Route): Future[Route] = {
    routeDao.getRoute(id).map {
      case None => throw new YaltaBaseException(s"Route with $id does not exist, can't update")
      case Some(route) => route
    }.flatMap {
      route => routeDao.updateRoute(change(route))
    }
  }

  def changeRouteDriver(routeId: Long, driverId: Long): Future[Route] = {
    updateRoute(routeId, route => new Route(route.getId, driverId, route.getRouteDate, route.getPoints, route.getFinished))
  }

  def changeRouteStatus(routeId: Long, finished: Boolean): Future[Route] = {
    updateRoute(routeId, route => new Route(route.getId, route.getDriverId, route.getRouteDate, route.getPoints, finished))
  }

  def changeRoutePoints(routeId: Long, points: util.ArrayList[RoutePoint]): Future[Route] = {
    updateRoute(routeId, route => new Route(route.getId, route.getDriverId, route.getRouteDate, points, route.getFinished))
  }

  def changeRouteDate(routeId: Long, date: DateTime): Future[Route] = {
    updateRoute(routeId, route => new Route(route.getId, route.getDriverId, date, route.getPoints, route.getFinished))
  }
}
