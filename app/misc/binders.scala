package misc

import java.sql.Timestamp
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

import play.api.mvc.QueryStringBindable

/**
 *  This allows binding custom type params in play routes
 */
object binders {

  /**
   * THis allows to bind to java.sql.Timestamp in play routes
   */
  implicit def queryStringBindable(implicit intBinder: QueryStringBindable[String]) = new QueryStringBindable[Timestamp] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Timestamp]] = {
      val dateString: Option[Seq[String]] = params.get(key)
      try {
//        TODO: rework this
        val ta: TemporalAccessor = DateTimeFormatter.ISO_INSTANT.parse(dateString.get.head)
        val i: Instant = Instant.from(ta)
        val ts = Timestamp.from(i)
        Some(Right(ts))
      } catch {
        case e: IllegalArgumentException => Option(Left(dateString.get.head))
      }
    }

    override def unbind(key: String, timestamp: Timestamp): String = {
      timestamp.toString
    }
  }
}
