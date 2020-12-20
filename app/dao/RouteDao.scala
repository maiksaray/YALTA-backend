package dao

import java.util

import common.{Point, Route}
import dao.implicits.DateTimeTransform._
import dao.implicits.IdTransform._
import dao.implicits.RouteTransform._
import dao.repo.{PointRepo, RoutePointRepo, RouteRepo}
import javax.inject.{Inject, Singleton}
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Singleton
class RouteDao @Inject()(pointRepo: PointRepo, routeRepo: RouteRepo, routePointRepo: RoutePointRepo)(implicit ec: ExecutionContext)
  extends BaseDao[mapping.Route, Long, RouteRepo](routeRepo)(ec) with Logging {

  def updateRoute(route: Route): Future[common.Route] = ???

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

  def createRoutePoints(points: util.List[common.RoutePoint], routeId: Long): Future[util.List[common.RoutePoint]] = {
    routeRepo.createRoutePointsWithId(
      points.asScala.map {
        p => mapping.RoutePoint(p.getId, routeId, p.getPoint.getId, p.getVisited)
      }.toList
    ).map {
      col =>
        col.map {
          rp => new common.RoutePoint(rp.id, null, rp.visited)
        }.toList.asJava
    }
  }

  def createRoutePointsWithPoints(points: util.List[common.RoutePoint], routeId: Long): Future[util.List[common.RoutePoint]] =
  //    Convert java collection to scala one for iteration
    points.asScala.map { p =>
      //      Map common.RoutePoint to pair of mapping.RoutePoint and underlying point
      //      Passing point here is needed to append it back to freshly created ROutePoint with Id
      (mapping.RoutePoint(p.getId, routeId, p.getPoint.getId, p.getVisited),
        p.getPoint)
    }.map {
      case (rp, p) =>
        //        Create RoutePoint in DB, and convert it back to common.RoutePoint with saved Point from before
        routeRepo.createRoutePoint(rp).map { created =>
          new common.RoutePoint(created.id, p, created.visited)
        }
      //        Since we got collection of Futures here, we'd want to fold those into Future of Collections
    }.foldLeft(
      //      Start folding with java collection so that we don't have to convert anymore
      Future.successful(List[common.RoutePoint]().asJava)
    ) {
      (acc: Future[util.List[common.RoutePoint]], f: Future[common.RoutePoint]) =>
        //        unwrap list/item from futures, append item and wrap it back
        for {
          list <- acc
          rp <- f
        } yield {
          list.add(rp)
          list
        }
    }


  def createRoute(route: common.Route): Future[common.Route] = {
    for {
      created <- routeRepo.create(route)
      points <- createRoutePointsWithPoints(route.getPoints, created.id.get)
    } yield {
      routeDbToModelWithPoints(created, points)
    }
  }

  def getRoute(id: Long): Future[Option[common.Route]] = {
    //    Oh myyy, I' sorry, I don't know rly, it's all slick...
    routeRepo.get(id).map { seq =>
      if (seq.isEmpty) {
        None
      } else {
        val firstRoute = seq.head._1
        val foldStart = new common.Route(firstRoute.id, firstRoute.driverID, firstRoute.date, new util.ArrayList[common.RoutePoint](), false)
        Some(
          seq.foldLeft[common.Route](foldStart) {
            (route, row) =>
              val point = new common.Point(row._3.id, row._3.lat, row._3.lon, row._3.name)
              val routePoint = new common.RoutePoint(row._2.id, point, row._2.visited)
              route.getPoints.add(routePoint)
              route
          })
      }
    }
  }
}
