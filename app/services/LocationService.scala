package services

import java.sql.Timestamp

import dao.LocationDao
import javax.inject.{Inject, Singleton}

import scala.concurrent.Future

@Singleton
class LocationService @Inject()(locationDao: LocationDao) {

  def create(lat: Double, lon: Double, userId: Long): Future[common.Location] =
    locationDao.create(lat, lon, userId)

  def postOfflineHistory(updates: List[common.OffsetedLocationUpdate], userId: Long): Future[Option[Int]] = {
    val now = new Timestamp(System.currentTimeMillis())
    val locations = updates.map {
      update => new common.Location(null, update.getLat, update.getLon, userId, new Timestamp(now.getTime - (update.getSecondsOffset * 1000L).toLong))
    }
    locationDao.bulkCreate(locations)
  }

  def getHistory(userId: Long, from: Timestamp, to: Timestamp): Future[Seq[common.Location]] = {
    locationDao.getRange(userId, from, to)
  }

}
