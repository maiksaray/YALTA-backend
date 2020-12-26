package services

import dao.LocationDao
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime

import scala.concurrent.Future

@Singleton
class LocationService @Inject()(locationDao: LocationDao) {

  def create(lat: Double, lon: Double, userId: Long): Future[common.Location] =
    locationDao.create(lat, lon, userId)

  def postOfflineHistory(updates: List[common.OffsetedLocationUpdate], userId: Long): Future[Option[Int]] = {
    val now = DateTime.now()
    val locations = updates.map {
      update =>
        val actualTs = now.minusMillis((update.getSecondsOffset * 1000).toInt)
        new common.Location(null, update.getLat, update.getLon, userId, actualTs)
    }
    locationDao.bulkCreate(locations)
  }

  def getHistory(userId: Long, from: DateTime, to: DateTime): Future[Seq[common.Location]] = {
    locationDao.getRange(userId, from, to)
  }

}
