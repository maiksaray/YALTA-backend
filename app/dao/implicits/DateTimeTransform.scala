package dao.implicits

import java.sql.Date
import java.time.Instant

import org.joda.time.{DateTime, DateTimeZone}

import scala.language.implicitConversions


object DateTimeTransform {

  implicit def dateTimeToInstant(dateTime: DateTime): Instant = Instant.ofEpochMilli(dateTime.getMillis)

  implicit def instantToDateTime(instant: Instant): DateTime =
    new DateTime(instant.toEpochMilli)
      .withZone(DateTimeZone.UTC)
      .withZoneRetainFields(DateTimeZone.getDefault)

  implicit def dateTimeToDate(dateTime: DateTime): Date = new java.sql.Date(dateTime.getMillis)

  implicit def dateToDateTime(instant: Date): DateTime = new DateTime(instant)
}
