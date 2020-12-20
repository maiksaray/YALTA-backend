package dao

import java.util

import dao.implicits.DateTimeTransform._
import dao.implicits.IdTransform._
import dao.implicits.RouteTransform._
import dao.mapping.RoutePoint
import dao.repo.{PointRepo, RoutePointRepo, RouteRepo}
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.Logging

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Singleton
class RouteDao @Inject()(pointRepo: PointRepo, routeRepo: RouteRepo, routePointRepo: RoutePointRepo)(implicit ec: ExecutionContext)
  extends BaseDao[mapping.Route, Long, RouteRepo](routeRepo)(ec) with Logging {
  def updatePointState(routeId: Long, pointIndex: Int, state: Boolean): Future[Unit] =
    routeRepo.updatePointState(routeId, pointIndex, state).flatMap {
      case 0 => Future.failed(new Exception("Can't update"))
      case _ => Future.successful(())
    }

  def getCurrentRoute(userId: Long): Future[Option[common.Route]] = {
    routeRepo.getRouteFor(userId, DateTime.now())
      .map(composeRoute)
  }

  def assignRoute(routeId: Long, driverId: Long): Future[Unit] =
    routeRepo.assignRoute(routeId, driverId).flatMap {
      case 0 => Future.failed(new Exception("can't assign"))
      case _ => Future.successful(())
    }

  def updateRoute(route: common.Route): Future[common.Route] = ???

  override def ensureExists(): Future[Unit] = {
    for {
      _ <- routeRepo.createTable()
      _ <- routeRepo.createPointsTable()
      _ <- routeRepo.createRoutePointsTable()
      _ <- routeRepo.createPoint(mapping.Point(None, 10.0, 10.0, "first"))
      _ <- routeRepo.createPoint(mapping.Point(None, 20.0, 20.0, "second"))
    } yield Future.successful(())
  }

  def createPoint(point: common.Point): Future[common.Point] =
    routeRepo.createPoint(point).map(pointDbToModel)

  def getPoint(id: Long): Future[Option[common.Point]] =
    routeRepo.getPoint(id).map {
      opt => opt.map(pointDbToModel)
    }

  def getPoints(): Future[Seq[common.Point]] =
    routeRepo.getAllPoints().map {
      f => f.map(pointDbToModel)
    }

  def updatePoint(point: common.Point): Future[common.Point] =
    routeRepo.updatePoint(point).flatMap {
      case 0 => Future.failed(new Exception("can't update point"))
      case _ => Future.successful(point)
    }

  def createRoutePoints(points: util.List[common.Point], routeId: Long): Future[util.List[common.RoutePoint]] = {
    val rps = points.asScala.zipWithIndex.map {
      case (p, i) => RoutePoint(None, routeId, p.getId, visited = false, i)
    }
    routeRepo.createRoutePointsWithId(rps).map { seq =>
      seq.map {
        rp => new common.RoutePoint(rp.id, points.get(rp.index), rp.visited, rp.index)
      }.asJava
    }
  }

  def createRoute(driverId: Long, routeDate: DateTime, points: util.List[common.Point]): Future[common.Route] = {
    val route = mapping.Route(None, nullableToOption(driverId), routeDate)
    for {
      created <- routeRepo.create(route)
      points <- createRoutePoints(points, created.id.get)
    } yield {
      routeDbToModelWithPoints(created, points)
    }
  }

  def getRoute(id: Long): Future[Option[common.Route]] = {
    routeRepo.getRoute(id)
      .map(composeRoute)
  }

  private def composeRoute(routeParts: Seq[(mapping.Route, RoutePoint, mapping.Point)]): Option[common.Route] = {
    routeParts match {
      case seq@Seq() =>
        val routePart = seq.head._1
        val points = seq.foldLeft(ListBuffer[common.RoutePoint]()) {
          (list, data) =>
            val pointData = data._3
            val rpData = data._2
            val point = new common.Point(pointData.id, pointData.lat, pointData.lon, pointData.name)
            list += new common.RoutePoint(rpData.id, point, rpData.visited, rpData.index)
        }
        Some(new common.Route(routePart.id, routePart.driverID, routePart.date, points.asJava, false))
      case _ => None
    }
  }
}
