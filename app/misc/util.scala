package misc

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat


trait CompletionMarker

case object CompletionMarker extends CompletionMarker

object DateHelper {
  def parse(dateString: String): Option[DateTime] =
    try {
      Some(
        ISODateTimeFormat.dateTimeParser()
          .parseDateTime(dateString)
      )
    } catch {
      case _: IllegalArgumentException => None
    }
}