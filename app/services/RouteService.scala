package services

import java.util

import com.google.inject.{Inject, Singleton}
import common.Route
import dao.RouteDao
import misc.{CompletionMarker, NotExistException, UpdateException}
import org.joda.time.DateTime
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RouteService @Inject()(routeDao: RouteDao)(implicit ec: ExecutionContext) extends Logging {

  private def updatePoint(id: Long, change: common.Point => common.Point): Future[common.Point] =
    routeDao.getPoint(id).map {
      case None => throw new NotExistException(s"Point with $id does not exist, can't update")
      case Some(point) => point
    }.flatMap {
      point => routeDao.updatePoint(change(point))
    }

  def getPoints(): Future[List[common.Point]] =
    routeDao.getPoints().map(_.toList)

  def getPoint(pointId: Long): Future[Option[common.Point]] =
    routeDao.getPoint(pointId)

  def createPoint(lat: Double, lon: Double, name: String): Future[common.Point] =
    createPoint(new common.Point(null, lat, lon, name))

  def createPoint(point: common.Point): Future[common.Point] =
    routeDao.createPoint(point)

  def changePointName(id: Long, name: String): Future[common.Point] =
    updatePoint(id, point => new common.Point(point.getId, point.getLat, point.getLon, name))

  def changePointLocation(pointId: Long, lat: Double, lon: Double): Future[common.Point] =
    updatePoint(pointId, point => new common.Point(point.getId, lat, lon, point.getName))

  def updateCurrentPointState(pointIndex: Int, userId: Long, state: Boolean): Future[CompletionMarker] =
    routeDao.getCurrentRouteId(userId).flatMap {
      case Some(id) => routeDao.updatePointState(id, pointIndex, state)
      case None => Future.failed(new UpdateException("Can't update point state for some reason"))
    }

  def updatePointState(routeId: Long, pointIndex: Int, userId: Long, state: Boolean): Future[CompletionMarker] =
    routeDao.updatePointState(routeId, pointIndex, state)

  def assignRoute(routeId: Long, driverId: Long): Future[CompletionMarker] =
    routeDao.assignRoute(routeId, driverId)

  def getCurrentRoute(userId: Long): Future[Option[common.Route]] =
    routeDao.getCurrentRouteId(userId).flatMap {
      case Some(id) => routeDao.getRoute(id)
      case None => Future.successful(None)
    }

  def getRoutes(userId: Long, from: DateTime, to: DateTime): Future[util.List[common.Route]] =
    routeDao.getRoutes(userId, from, to)

  def getRoutes(from:DateTime, to:DateTime): Future[List[common.Route]] =
    routeDao.getRoutes(from, to)

  def getRoute(id: Long): Future[Option[common.Route]] =
    routeDao.getRoute(id)

  def createRoute(driverId: Long, routeDate: DateTime, points: util.List[common.Point]): Future[Route] =
    routeDao.createRoute(driverId, routeDate, points)


}
