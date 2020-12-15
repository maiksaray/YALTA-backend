package misc

import java.sql.Timestamp
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.mvc.QueryStringBindable

/**
 * This allows binding custom type params in play routes
 */
object binders {

  /**
   * THis allows to bind to java.sql.Timestamp in play routes
   */
  implicit def queryStringBindable(implicit intBinder: QueryStringBindable[String]) = new QueryStringBindable[DateTime] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, DateTime]] = {
      val dateString: Option[Seq[String]] = params.get(key)
      try {
        val parser = ISODateTimeFormat.dateTimeParser()
        val dt = parser.parseDateTime(dateString.get.head)
        Some(Right(dt))
      } catch {
        case e: IllegalArgumentException => Option(Left(dateString.get.head))
      }
    }

    override def unbind(key: String, timestamp: DateTime): String = {
      timestamp.toString
    }
  }
}
