package services

import java.util

import com.google.inject.{Inject, Singleton}
import common.Route
import dao.RouteDao
import exceptions.YaltaBaseException
import misc.camunda.Fail
import org.joda.time.DateTime
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RouteService @Inject()(routeDao: RouteDao,
                             userService: UserService,
                             camunda: CamundaService)(implicit ec: ExecutionContext) extends Logging {

  def getPoints(): Future[List[common.Point]] =
    routeDao.getPoints().map {
      seq => seq.toList
    }

  def getPoint(pointId: Long): Future[Option[common.Point]] =
    routeDao.getPoint(pointId)

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

  def updateCurrentPointState(pointIndex: Int, userId: Long, state: Boolean): Future[Unit] =
    routeDao.getCurrentRouteId(userId).map {
      case Some(id) => routeDao.updatePointState(id, pointIndex, state)
      case None => Future.failed(new Exception(""))
    }

  def updatePointState(routeId: Long, pointIndex: Int, userId: Long, state: Boolean): Future[Unit] =
    routeDao.updatePointState(routeId, pointIndex, state).flatMap {
      _ =>
        isRouteCompleted(routeId).map {
          case true =>
            if (camunda.enabled) {
              userService.get(userId).map {
                case Some(user) =>
                  camunda.completeRoute(routeId, user)
                  ()
                case None => ()
              }
            }
          case _ => ()
        }
    }

  def assignRoute(routeId: Long, driverId: Long, admin: common.User): Future[Unit] =
    routeDao.assignRoute(routeId, driverId).flatMap {
      _ =>
        if (camunda.enabled) {
          userService.get(driverId).map {
            case Some(driver) =>
              camunda.assignRoute(routeId, driver, admin)
            case None => Fail
          }.map(_ => ())
        } else {
          Future.successful(())
        }
    }

  def isRouteCompleted(routeId: Long): Future[Boolean] =
    routeDao.getRouteState(routeId)

  def getCurrentRoute(userId: Long): Future[Option[common.Route]] =
    routeDao.getCurrentRouteId(userId).flatMap {
      case Some(id) => routeDao.getRoute(id)
      case None => Future.successful(None)
    }

  def getRoute(id: Long): Future[Option[common.Route]] =
    routeDao.getRoute(id)

  def createRoute(driverId: Long, routeDate: DateTime, points: util.List[common.Point]): Future[Route] =
    routeDao.createRoute(driverId, routeDate, points)
      .map(postprocessRoute)

  private def postprocessRoute(route: Route): Route = {
    if (camunda.enabled) {
      camunda.startProcess(route.getId)
    }
    route
  }
}
