package dao.implicits

import java.time.Instant

import dao.mapping.Location
import IdTransform._
import org.joda.time.DateTime

import scala.language.implicitConversions

object LocationTransform {

  implicit def dateTimeToInstant(dateTime: DateTime): Instant = Instant.ofEpochMilli(dateTime.getMillis)

  implicit def instantToDateTime(instant: Instant): DateTime = new DateTime(instant.toEpochMilli)

  implicit def locationDbToModel(location: Location): common.Location = {
    new common.Location(location.id, location.lat, location.lon, location.userId, location.timestamp)
  }

  implicit def locationModeltoDb(location: common.Location): Location = {
    Location(location.getId, location.getLat, location.getLon, location.getUserId, location.getTimestamp)
  }
}
