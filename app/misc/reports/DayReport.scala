package misc.reports

import java.text.SimpleDateFormat
import java.util.GregorianCalendar

import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.ReportChart
import com.sysalto.report.ReportTypes.ReportCheckpoint
import com.sysalto.report.reportTypes.{CellAlign, GroupUtil, RFont, RFontFamily, ReportColor, ReportPageOrientation}
import com.sysalto.report.util._
import org.joda.time.DateTime

import scala.collection.mutable.ListBuffer

case class PointData(name: String, finished: Boolean, ts: DateTime)

case class RouteData(name: String, driver: String, finished: Boolean, pointsData: List[PointData])

class DayReport {


}

