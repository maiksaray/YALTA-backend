package services

import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.reportTypes.{ReportColor, ReportPageOrientation}
import com.sysalto.report.util._
import common.{Point, RoutePoint}
import javax.inject.Inject
import misc.reports.{PointData, RouteData}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._


class ReportService @Inject()(val userService: UserService,
                              val routeService: RouteService,
                              val mapService: MapService)
                             (implicit ec: ExecutionContext) {

  implicit val pdfFactory: PdfFactory = new PdfNativeFactory()

  def margin = 20f //TODO: get this from config?

  def reportMapHeight = 150

  def generateDayReport(date: DateTime): Future[String] = {
    getReportData(date, true).map { data =>
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

        val headerRow = ReportRow(margin * 2, report.pageLayout.width - margin * 2, List(
          Column("Name", Flex(1)),
          Column("Date", Flex(1))
        ))

        report.print(List(
          ReportCell("Yalta Route report".bold().size(14)).inside(headerRow, "Name"),
          ReportCell(date.toString("DD MM YYYY").bold().size(14)).rightAlign().inside(headerRow, "Date")
        ))

        report.nextLine()
        report.line().from(margin, report.getY).to(report.pageLayout.width - margin).draw()
    }

    report.footerFct = {
      case (pgNbr, pgMax) =>
        report.setYPosition(report.pageLayout.height - report.lineHeight * 3)
        report.line().from(margin, report.getY).to(report.pageLayout.width - margin).draw()
        report.nextLine()
        report print (ReportCell(s"Generated ${date.toString("DD MM YYYY")}").leftAlign() inside ReportMargin(margin, report.pageLayout.width))
        report.nextLine()
        report print (ReportCell(s"Page $pgNbr of $pgMax".bold()).rightAlign() inside ReportMargin(0, report.pageLayout.width - margin))
    }

  }

  private def setStyle(report: Report) = {
    report.setHeaderSize = { pgNbr =>
      if (pgNbr == 1) 90f else 90f
    }

    report.setFooterSize = { _ =>
      30f
    }

    report.newPageFct = _ => drawbackgroundImage(report)
  }

  def renderRoute(report: Report, routeData: RouteData) = {
    report.nextLine(3)

    val routeRow = ReportRow(margin, report.pageLayout.width - margin, List(
      Column("Route", Flex(1)),
      Column("Driver", Flex(1)),
      Column("Status", Flex(1))
    ))


    if (report.lineLeft <= routeData.pointsData.length + 3) {
      report.nextPage()
    }

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

    routeData.mapfile match {
      case Some(mapfile) =>
        if (report.lineLeft < 8) {
          report.nextPage()
        }
        val yOffset = report.pageLayout.height - report.getCurrentPosition.y + reportMapHeight
        report.drawImage(mapfile,
          margin, yOffset,
          report.pageLayout.width - margin * 2, reportMapHeight)
        report.setYPosition(yOffset)
      case None => ()
    }
  }

  private def renderPoint(report: Report, pointData: PointData) = {
    if (report.lineLeft < 2) {
      report.nextPage()
    }
    report.nextLine()
    val pointRow = ReportRow(margin * 2, report.pageLayout.width - margin, List(
      Column("Index", Flex(1)),
      Column("Point", Flex(2)),
      Column("State", Flex(2)),
      Column("Time", Flex(2))
    ))
    report.print(List(
      ReportCell((pointData.index + 1).toString).inside(pointRow, "Index"),
      ReportCell(pointData.name).inside(pointRow, "Point"),
      ReportCell(if (pointData.finished) "Visited" else "Not Visited").inside(pointRow, "State"),
      ReportCell(if (pointData.finished) pointData.ts.toString("HH:mm:ss") else "N/A").inside(pointRow, "Time")

    ))
  }

  private def renderPreContent(report: Report, date: DateTime, data: List[RouteData]) = {
    report.nextLine(3)
  }

  private def renderReport(report: Report, date: DateTime, data: List[RouteData]) = {
    setStyle(report)

    setRunningSections(report, date)
    report.start()

    renderPreContent(report, date, data)

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

  private def getReportData(date: DateTime, withMap: Boolean = false): Future[List[RouteData]] = {
    routeService.getRoutes(date.minusDays(1), date.plusDays(1)).flatMap { list =>
      Future.sequence(
        list.map { route =>
          val routePoints = route.getPoints.asScala
            .sortBy(_.getIndex).toList
          userService.get(route.getDriverId).flatMap {
            case Some(user) =>
              (if (withMap) {
                getMapPic(date, user.getId, routePoints)
                  .map(Some.apply)
                  .map(file => (user.getName, file))
              } else {
                Future.successful((user.getName, None))
              })
            case None =>
              Future.successful(("Unassigned", None))
          }.map {
            case (username, maybeFile) =>
              RouteData(
                route.getId.toString,
                username,
                route.getFinished,
                route.getPoints.asScala.map { point =>
                  PointData(point.getIndex, point.getPoint.getName, point.getVisited, point.getUpdated)
                }.toList,
                maybeFile
              )
          }
        }
      )
    }
  }

  def getMapPic(date: DateTime, id: Long, points: List[RoutePoint] = List.empty): Future[String] = {
    mapService.createMap(id,
      date.withTime(0, 0, 0, 0),
      date.plusDays(1).withTime(0, 0, 0, 0),
      650, reportMapHeight,
      points)
  }
}
