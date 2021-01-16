package misc

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
      (for {
        dateStrings <- params.get(key)
        dateString <- dateStrings.headOption
        date <- DateHelper.parse(dateString)
      } yield date) match {
        case Some(dt) => Some(Right(dt))
        case None => Some(Left(params(key).head))
      }
    }

    override def unbind(key: String, timestamp: DateTime): String = {
      timestamp.toString
    }
  }
}
