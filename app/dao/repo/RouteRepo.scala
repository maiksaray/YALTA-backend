package dao.repo

import java.sql.Date

import com.byteslounge.slickrepo.meta.Keyed
import dao.mapping.{Point, Route, RoutePoint}
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.BaseTypedType

import scala.concurrent.{ExecutionContext, Future}

class RouteRepo @Inject()(configProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends CommonRepo[Route, Long](configProvider) {

  import dbConfig._
  import profile.api._

  override type TableType = Routes

  override def pkType = implicitly[BaseTypedType[Long]]

  override def tableQuery = TableQuery[Routes]

  class Routes(tag: Tag) extends Table[Route](tag, "routes") with Keyed[Long] {
    override def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def driverId = column[Long]("driver")

    def date = column[Date]("date")

    override def * = (id.?, driverId.?, date) <> ((Route.apply _).tupled, Route.unapply)
  }

  class RoutePoints(tag: Tag) extends Table[RoutePoint](tag, "route_point") with Keyed[Long] {
    override def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def routeId = column[Long]("routeId")

    def pointId = column[Long]("pointId")

    def visited = column[Boolean]("visisted")

    def index = column[Int]("index")

    override def * = (id.?, routeId, pointId, visited, index) <> ((RoutePoint.apply _).tupled, RoutePoint.unapply)
  }

  class Points(tag: Tag) extends Table[Point](tag, "points") with Keyed[Long] {
    override def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def lat = column[Double]("lat")

    def lon = column[Double]("lon")

    def name = column[String]("name")

    def * = (id.?, lat, lon, name) <> ((Point.apply _).tupled, Point.unapply)
  }

  def routePoints = TableQuery[RoutePoints]

  def points = TableQuery[Points]

  //region points

  def createPointsTable(): Future[Unit] = db.run {
    points.schema.createIfNotExists
  }

  def createRoutePointsTable(): Future[Unit] = db.run {
    routePoints.schema.createIfNotExists
  }

  override def createTable(): Future[Unit] = db.run {
    tableQuery.schema.createIfNotExists
  }

  def createPoint(point: Point): Future[Point] = db.run {
    ((points returning points.map(_.id)) += point).map(point.withId)
  }

  def getPoint(id: Long): Future[Option[Point]] = db.run {
    points.filter(_.id === id).result.headOption
  }

  def getAllPoints(): Future[Seq[Point]] = db.run {
    points.result
  }

  private def pointUdateQuery(pointId: Option[Long]) =
    for {p <- points if p.id === pointId} yield (p.lat, p.lon, p.name)

  def updatePoint(point: Point): Future[Int] = db.run {
    pointUdateQuery(point.id).update(point.lat, point.lon, point.name)
  }

  //endregion

  //region routePoints

  def createRoutePoint(routePoint: RoutePoint) = db.run {
    ((routePoints returning routePoints.map(_.id)) += routePoint).map(routePoint.withId)
  }

  def createRoutePounts(points: Iterable[RoutePoint]): Future[Option[Int]] = db.run {
    routePoints ++= points
  }

  def RoutePointBulkInsertQuery =
    (routePoints returning routePoints.map(_.id)).into((routePoint, id) => routePoint.withId(id))

  def createRoutePointsWithId(points: Iterable[RoutePoint]): Future[Seq[RoutePoint]] = db.run {
    RoutePointBulkInsertQuery ++= points
  }

  def pointStateUpdateQuery(routeId: Long, index: Int) =
    for {rp <- routePoints if rp.routeId === routeId && rp.index === index} yield (rp.visited)

  def updatePointState(routeId: Long, pointIndex: Int, state: Boolean) = db.run {
    pointStateUpdateQuery(routeId, pointIndex).update(state)
  }

  //endregion

  def assignQuery(id: Option[Long]) =
    for {r <- tableQuery if r.id === id} yield r.driverId

  def assignRoute(routeId: Long, driverId: Long): Future[Int] = db.run {
    assignQuery(Some(routeId)).update(driverId)
  }

  private def userDateRouteQuery(userId: Long, time: Date) =
    for {
      route <- tableQuery if route.driverId === userId && route.date === time
      routePoint <- routePoints if route.id === routePoint.routeId
      point <- points if routePoint.pointId === point.id
    } yield (route.id, route.driverId, route.date,
      routePoint.id, routePoint.visited, routePoint.index,
      point.id, point.lat, point.lon, point.name)

  def getRouteFor(userId: Long, time: Date): Future[Seq[(Route, RoutePoint, Point)]] = db.run {
    userDateRouteQuery(userId, time).result.map {
      _.map {
        case (rid, driverId, date,
        rpid, visited, index,
        pid, lat, lon, name) =>
          (Route(Some(rid), Some(driverId), date),
            RoutePoint(Some(rpid), 0, 0, visited, index),
            Point(Some(pid), lat, lon, name))
      }
    }
  }

  def getRouteIdFor(userId: Long, time: Date): Future[Option[Long]] = db.run {
    tableQuery
      .filter(_.driverId === userId)
      .filter(_.date === time)
      .map(_.id)
      .result.headOption
  }

  private def getRouteWithPointsQuery(id: Long) = {
    for {
      route <- tableQuery if route.id === id
      routePoint <- routePoints if route.id === routePoint.routeId
      point <- points if routePoint.pointId === point.id
    } yield (route.id, route.driverId, route.date,
      routePoint.id, routePoint.visited, routePoint.index,
      point.id, point.lat, point.lon, point.name)
  }

  def getRoute(id: Long): Future[Seq[(Route, RoutePoint, Point)]] = db.run {
    //    TODO: make custom mapping instead of this shiet with tuples
    getRouteWithPointsQuery(id).result.map {
      _.map {
        case (rid, driverId, date,
        rpid, visited, index,
        pid, lat, lon, name) =>
          (Route(Some(rid), Some(driverId), date),
            RoutePoint(Some(rpid), 0, 0, visited, index),
            Point(Some(pid), lat, lon, name))
      }
    }
  }

  private def userRoutesQuery(userId: Long, from: Date, to: Date) =
    for {
      route <- tableQuery if route.driverId === userId && route.date >= from && route.date <= to
      routePoint <- routePoints if route.id === routePoint.routeId
      point <- points if routePoint.pointId === point.id
    } yield (route.id, route.driverId, route.date,
      routePoint.id, routePoint.visited, routePoint.index,
      point.id, point.lat, point.lon, point.name)

  def getRoutes(userId: Long, from: Date, to: Date): Future[Seq[(Route, RoutePoint, Point)]] = db.run {
    userRoutesQuery(userId, from, to).result.map {
      _.map {
        case (rid, driverId, date,
        rpid, visited, index,
        pid, lat, lon, name) =>
          (Route(Some(rid), Some(driverId), date),
            RoutePoint(Some(rpid), 0, 0, visited, index),
            Point(Some(pid), lat, lon, name))
      }
    }
  }
}
