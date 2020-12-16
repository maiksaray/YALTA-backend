package dao.implicits

import java.time.Instant

import org.joda.time.DateTime

import scala.language.implicitConversions


object DateTimeTransform {

  implicit def dateTimeToInstant(dateTime: DateTime): Instant = Instant.ofEpochMilli(dateTime.getMillis)

  implicit def instantToDateTime(instant: Instant): DateTime = new DateTime(instant.toEpochMilli)
}
