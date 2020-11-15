package dao

import java.sql.Timestamp

import dao.mapping.Location
import dao.repo.LocationRepo
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import implicits.LocationTransform._

@Singleton
class LocationDao @Inject()(repo: LocationRepo)(implicit ec: ExecutionContext)
  extends BaseDao[Location, Long, LocationRepo](repo)(ec) {

  def checkin(lat: Double, lon: Double, userId: Long): Future[common.Location] = {
    checkin(Location(None, lat, lon, userId, new Timestamp(System.currentTimeMillis())))
  }

  def checkin(location: common.Location): Future[common.Location] = {
    val eventualLocation = repo.create(location)
    eventualLocation.map(locationDbToModel)
  }

}
