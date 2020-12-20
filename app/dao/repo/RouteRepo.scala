package dao.repo

import java.time.Instant

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import dao.mapping.{Point, Route, RoutePoint}
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.BaseTypedType

import scala.concurrent.{ExecutionContext, Future}

class RouteRepo @Inject()(routePointRepo: RoutePointRepo,
                          pointRepo: PointRepo,
                          configProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends CommonRepo[Route, Long](configProvider) {

  import dbConfig._
  import profile.api._

  override type TableType = Routes

  override def pkType = implicitly[BaseTypedType[Long]]

  override def tableQuery = TableQuery[Routes]

  class Routes(tag: Tag) extends Table[Route](tag, "routes") with Keyed[Long] {
    override def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def driverId = column[Long]("driver")

    def date = column[Instant]("date")

    override def * = (id.?, driverId.?, date) <> ((Route.apply _).tupled, Route.unapply)
  }

  class RoutePoints(tag: Tag) extends Table[RoutePoint](tag, "route_point") with Keyed[Long] {
    override def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def routeId = column[Long]("routeId")

    def pointId = column[Long]("pointId")

    def visited = column[Boolean]("visisted")

    override def * = (id.?, routeId, pointId, visited) <> ((RoutePoint.apply _).tupled, RoutePoint.unapply)
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

  def createPointsTable(): Future[Unit] = db.run {
    points.schema.createIfNotExists
  }

  def createRoutePointsTable(): Future[Unit] = db.run {
    routePoints.schema.createIfNotExists
  }

  override def createTable(): Future[Unit] = db.run {
    tableQuery.schema.createIfNotExists
  }

  //  def routeWithPointsMapping =

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


  def createRoutePoint(routePoint: RoutePoint) = db.run {
    ((routePoints returning routePoints.map(_.id)) += routePoint).map(routePoint.withId)
  }

  def createRoutePounts(points: Iterable[RoutePoint]): Future[Option[Int]] = db.run {
    routePoints ++= points
  }

  def bulkInsertQuery =
    (routePoints returning routePoints.map(_.id)).into((routePoint, id) => routePoint.withId(id))

  def createRoutePointsWithId(points: Iterable[RoutePoint]): Future[Seq[RoutePoint]] = db.run {
    bulkInsertQuery ++= points
  }

  def get(id: Long) = db.run {
    //    TODO: make custom mapping instead of this shiet with tuples
    getRouteWithPointsQuery.result
      .map {
        _.map {
          case (rid, driverId, date,
          rpid, visited,
          pid, lat, lon, name) =>
            (Route(Some(rid), Some(driverId), date),
              RoutePoint(Some(rpid), 0, 0, visited),
              Point(Some(pid), lat, lon, name))
        }
      }
  }

  private def getRouteWithPointsQuery = {
    for {
      r <- tableQuery
      rp <- routePoints if r.id === rp.routeId
      p <- points if r.id === p.id
    } yield (r.id, r.driverId, r.date,
      rp.id, rp.visited,
      p.id, p.lat, p.lon, p.name)
  }
}

//Not really used
@Deprecated
class RoutePointRepo @Inject()(configProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends CommonRepo[RoutePoint, Long](configProvider) {

  import dbConfig._
  import profile.api._

  override type TableType = RoutePoints

  override def pkType = implicitly[BaseTypedType[Long]]

  override def tableQuery = TableQuery[RoutePoints]


  class RoutePoints(tag: Tag) extends Table[RoutePoint](tag, "route_point") with Keyed[Long] {
    override def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def routeId = column[Long]("routeId")

    def pointId = column[Long]("pointId")

    def visited = column[Boolean]("visisted")

    override def * = (id.?, routeId, pointId, visited) <> ((RoutePoint.apply _).tupled, RoutePoint.unapply)
  }

}

//Not really used...
@Deprecated
class PointRepo @Inject()(configProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends CommonRepo[Point, Long](configProvider) {

  import dbConfig._
  import profile.api._

  override type TableType = Points

  override def pkType = implicitly[BaseTypedType[Long]]

  override def tableQuery = TableQuery[Points]

  class Points(tag: Tag) extends Table[Point](tag, "points") with Keyed[Long] {
    override def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def lat = column[Double]("lat")

    def lon = column[Double]("lon")

    def name = column[String]("name")

    def * = (id.?, lat, lon, name) <> ((Point.apply _).tupled, Point.unapply)
  }

}
