package dao

import java.util

import dao.implicits.DateTimeTransform._
import dao.implicits.IdTransform._
import dao.implicits.RouteTransform._
import dao.mapping.RoutePoint
import dao.repo.RouteRepo
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.Logging

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Singleton
class RouteDao @Inject()(routeRepo: RouteRepo)(implicit ec: ExecutionContext)
  extends BaseDao[mapping.Route, Long, RouteRepo](routeRepo)(ec) with Logging {

  override def ensureExists(): Future[Unit] = {
    (for {
      _ <- routeRepo.createTable()
      _ <- routeRepo.createPointsTable()
      _ <- routeRepo.createRoutePointsTable()
    } yield Future.successful(())).flatMap { _ =>
      routeRepo.getPoint(1).flatMap {
        case None =>
          for {
            p1 <- createPoint(mapping.Point(None, 10.0, 10.0, "first"))
            p2 <- createPoint(mapping.Point(None, 20.0, 20.0, "second"))
            _ <- createRoute(2L, DateTime.now(), List(p1, p2).asJava)
          } yield Future.successful(())
        case _ => Future.successful(())
      }
    }
  }

  //region point

  def createPoint(point: common.Point): Future[common.Point] =
//    TODO: check name exists
    routeRepo.createPoint(point).map(pointDbToModel)

  def getPoint(id: Long): Future[Option[common.Point]] =
    routeRepo.getPoint(id).map {
      opt => opt.map(pointDbToModel)
    }

  def getPoints(): Future[Seq[common.Point]] =
    routeRepo.getAllPoints().map {
      points => points.map(pointDbToModel)
    }

  def updatePoint(point: common.Point): Future[common.Point] =
    routeRepo.updatePoint(point).flatMap {
      case 0 => Future.failed(new Exception("can't update point"))
      case _ => Future.successful(point)
    }

  //endregion

  //region RoutePoints

  def createRoutePoints(points: util.List[common.Point], routeId: Long): Future[util.List[common.RoutePoint]] = {
    val rps = points.asScala.zipWithIndex.map {
      case (point, index) => RoutePoint(None, routeId, point.getId, visited = false, index)
    }
    routeRepo.createRoutePointsWithId(rps).map { seq =>
      seq.map {
        rp => new common.RoutePoint(rp.id, points.get(rp.index), rp.visited, rp.index)
      }.asJava
    }
  }

  def updatePointState(routeId: Long, pointIndex: Int, state: Boolean): Future[Unit] =
    routeRepo.updatePointState(routeId, pointIndex, state).flatMap {
      case 0 => Future.failed(new Exception("Can't update"))
      case _ => Future.successful(())
    }

  //endregion

  def createRoute(driverId: Long, routeDate: DateTime, points: util.List[common.Point]): Future[common.Route] = {
    logger.info(s"Creating route for $driverId and $routeDate")
    routeRepo.getRouteIdFor(driverId, routeDate).flatMap {
      //    TODO: Move this to service
      case None =>
        for {
          created <- routeRepo.create(mapping.Route(None, nullableToOption(driverId), routeDate))
          points <- createRoutePoints(points, created.id.get)
        } yield {
          routeDbToModelWithPoints(created, points)
        }
      case _ =>
        logger.info(s"Can't create route for $driverId and $routeDate, it exists")
        Future.failed(new Exception("Route Exists"))
    }
  }

  private def composeRoute(routeParts: Seq[(mapping.Route, RoutePoint, mapping.Point)]): Option[common.Route] = {
    routeParts match {
      case seq@Seq() =>
        None
      case seq =>
        val routePart = seq.head._1
        val points = seq.foldLeft(ListBuffer[common.RoutePoint]()) {
          (list, data) =>
            val pointData = data._3
            val rpData = data._2
            val point = new common.Point(pointData.id, pointData.lat, pointData.lon, pointData.name)
            list += new common.RoutePoint(rpData.id, point, rpData.visited, rpData.index)
        }
        val finished = points.forall(_.getVisited)
        Some(new common.Route(routePart.id, routePart.driverID, routePart.date, points.asJava, finished))
    }
  }

  def getRoute(id: Long): Future[Option[common.Route]] = {
    routeRepo.getRoute(id)
      .map(composeRoute)
  }

  def getCurrentRouteId(userId: Long): Future[Option[Long]] = {
    routeRepo.getRouteIdFor(userId, DateTime.now())
  }

  def assignRoute(routeId: Long, driverId: Long): Future[Unit] =
//    TODO: check that new driver doesn't have route for same date
    routeRepo.assignRoute(routeId, driverId).flatMap {
      case 0 => Future.failed(new Exception("can't assign"))
      case _ => Future.successful(())
    }
}
