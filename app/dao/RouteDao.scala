package dao

import common.{Point, Route, RoutePoint}
import javax.inject.{Inject, Singleton}
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RouteDao @Inject()()(implicit ec: ExecutionContext)
    extends Logging {
  def createPoint(point: Point): Future[Point] = ???

  def getPoint(id: Long): Future[Option[common.Point]] = ???

  def updatePoint(point: Point):Future[common.Point] = ???

  def createRoutePoint(routePoint: RoutePoint): Future[RoutePoint] = ???

  def getRoutePoint(id: Long): Future[Option[RoutePoint]] = ???

  def updateRoutePoint(point: RoutePoint): Future[RoutePoint] = ???

  def createRoute(route: Route): Future[Route] = ???

  def getRoute(id: Long): Future[Option[Route]] = ???

  def updateRoute(route: Route): Future[Route] = ???
}
