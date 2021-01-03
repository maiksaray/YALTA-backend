package services

import javax.inject.Inject
import misc.reports.{PointData, RouteData}
import org.joda.time.DateTime
import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.ReportChart
import com.sysalto.report.ReportTypes.ReportCheckpoint
import com.sysalto.report.reportTypes.{CellAlign, GroupUtil, RFont, RFontFamily, ReportColor, ReportPageOrientation}
import com.sysalto.report.util._
import org.joda.time.DateTime


import scala.jdk.CollectionConverters._
import scala.concurrent.{ExecutionContext, Future}


class ReportService @Inject()(val userService: UserService,
                              val routeService: RouteService)
                             (implicit ec: ExecutionContext) {

  implicit val pdfFactory: PdfFactory = new PdfNativeFactory()

  def margin = 20 //TODO: get this from config?

  def generateDayReport(date: DateTime): Future[String] = {
    getReportData(date).map { data =>
      createReport(date, data)
    }
  }

  private def drawbackgroundImage(report: Report): Unit = {
    report.rectangle().from(0, 0).to(report.pageLayout.width, report.pageLayout.height)
      .verticalShade(ReportColor(255, 255, 255), ReportColor(255, 255, 180)).draw()
  }

  private def setRunningSections(report: Report, date: DateTime) = {
    report.headerFct = {
      case (_, _) =>
        report.setYPosition(30)
        report.setYPosition(30)
        val row = ReportRow(margin, report.pageLayout.width - margin, List(
          Column("column1", Flex(1)),
          Column("column2", Flex(1)),
          Column("column3", Flex(1))))
        val column1 = row.getColumnBound("column1")
        val column2 = row.getColumnBound("column2")
        val column3 = row.getColumnBound("column3")

        val h_column1 = ReportCell("Type of Account".bold()).leftAlign() inside column1
        val h_column2 = ReportCell("Your account number".bold()).leftAlign() inside column2
        val h_column3 = ReportCell("Your investment statement".bold()).rightAlign() inside column3
        val hrow = List(h_column1, h_column2, h_column3)
        report.print(hrow)
        report.nextLine()
        val str = date.toString("DD MM YYYY")
        val r_column1 = ReportCell("Group Registered Retirement Saving Plan").leftAlign() inside column1
        val r_column2 = ReportCell("123456789").leftAlign() inside column2
        val r_column3 = ReportCell(str).rightAlign() inside column3
        val rrow = List(r_column1, r_column2, r_column3)
        report.print(rrow)
        report.nextLine(2)
        report.line().from(margin, report.getY).to(report.pageLayout.width - margin).draw()
    }

    report.footerFct = {
      case (pgNbr, pgMax) =>
        report.setYPosition(report.pageLayout.height - report.lineHeight * 3)
        report.line().from(margin, report.getY).to(report.pageLayout.width - margin).draw()
        report.nextLine()
        report print (ReportCell(date.toString("DD MM YYYY")).leftAlign() inside ReportMargin(margin, report.pageLayout.width))
        report.nextLine()
        report print (ReportCell(s"Page $pgNbr of $pgMax".bold()).rightAlign() inside ReportMargin(0, report.pageLayout.width - margin))
    }

  }

  private def setStyle(report: Report) = {
    // set page header size(height) at 50 and 0 (no page header) for the first page.
    report.setHeaderSize = { pgNbr =>
      if (pgNbr == 1) 0f else 80f
    }

    // set footer size(hight) at 30 for all pages.
    report.setFooterSize = { _ =>
      30f
    }

    // draw background image before rendering anything
    report.newPageFct = _ => drawbackgroundImage(report)
  }

  def renderRoute(report: Report, routeData: RouteData) = {
    report.nextLine(3)
    val routeRow = ReportRow(margin, report.pageLayout.width - margin, List(
      Column("Route", Flex(1)),
      Column("Driver", Flex(1)),
      Column("Status", Flex(1))
    ))
    val routeState = if (routeData.finished) "Completed" else "In Progress"
    report.print(List(
      ReportCell(s"Route id: ${routeData.name}").inside(routeRow, "Route"),
      ReportCell(s"Driver login: ${routeData.driver}").inside(routeRow, "Driver"),
      ReportCell(s"Status: $routeState").inside(routeRow, "Status")
    ))
    report.nextLine()
    report.line().from(margin, report.getY).to(report.pageLayout.width - margin).draw()
    report.nextLine()

    val pointTableHeaderRow = ReportRow(margin * 2, report.pageLayout.width - margin, List(
      Column("Point", Flex(1)),
      Column("State", Flex(1)),
      Column("Time", Flex(1))
    ))
    report.print(List(
      ReportCell("Point name").inside(pointTableHeaderRow, "Point"),
      ReportCell("State").inside(pointTableHeaderRow, "State"),
      ReportCell("Visited at").inside(pointTableHeaderRow, "Time")
    ))
    for (pointData <- routeData.pointsData) {
      renderPoint(report, pointData)
    }
  }

  private def renderPoint(report: Report, pointData: PointData) = {
    report.nextLine()
    val pointRow = ReportRow(margin * 2, report.pageLayout.width - margin, List(
      Column("Point", Flex(1)),
      Column("State", Flex(1)),
      Column("Time", Flex(1))
    ))
    report.print(List(
      ReportCell(pointData.name).inside(pointRow, "Point"),
      ReportCell(if (pointData.finished) "Visited" else "Not Visited").inside(pointRow, "State"),
      ReportCell(if (pointData.finished) pointData.ts.toString("hh:mm:ss") else "N/A").inside(pointRow, "Time")
    ))
  }

  private def renderHeader(report: Report, date: DateTime, data: List[RouteData]) = {
    report.nextLine(2)
    val headerRow = ReportRow(margin * 2, report.pageLayout.width - margin * 2, List(
      Column("Name", Flex(1)),
      Column("Date", Flex(1))
    ))
    report.print(List(
      ReportCell("Yalta Route report".bold().size(15)).inside(headerRow, "Name"),
      ReportCell(date.toString("DD MM YYYY").bold().size(15)).rightAlign().inside(headerRow, "Date")
    ))
    report.nextLine()
  }

  private def renderReport(report: Report, date: DateTime, data: List[RouteData]) = {
    setStyle(report)

    setRunningSections(report, date)
    report.start()

    renderHeader(report, date, data)

    for (routeData <- data.sortBy(_.name)) {
      renderRoute(report, routeData)
    }
    report.render()
  }

  private def createReport(date: DateTime, data: List[RouteData]): String = {
    val filename = "YaltaDayReport.pdf"

    val report1 = Report(filename, ReportPageOrientation.PORTRAIT)

    renderReport(report1, date, data)

    filename
  }

  private def getReportData(date: DateTime): Future[List[RouteData]] = {
    routeService.getRoutes(date.minusDays(1), date.plusDays(1)).flatMap { list =>
      Future.sequence(
        list.map { route =>
          userService.get(route.getDriverId).map {
            case Some(user) =>
              RouteData(route.getId.toString,
                user.getName,
                route.getFinished,
                route.getPoints.asScala.map { point =>
                  PointData(point.getPoint.getName, point.getVisited,
//              TODO: WORK AROUND THIS, PASS INTERNAL UPDATED DATETIME
                    DateTime.now())
//              TODO: ----------------
                }.toList)
          }
        }
      )
    }
  }
}
