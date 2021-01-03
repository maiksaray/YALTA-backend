package controllers

import common.{Admin, Driver}
import dao.SessionDao
import javax.inject.Inject
import misc.reports.{DayReport, PointData, RouteData}
import org.joda.time.DateTime
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import security.UserAction
import services.{ReportService, RouteService}

import scala.concurrent.{ExecutionContext, Future}

class ReportController @Inject()(routeService: RouteService,
                                 sessionDao: SessionDao,
                                 reportService: ReportService,
                                 cc: MessagesControllerComponents,
                                 override val userAction: UserAction
                                )(implicit ec: ExecutionContext)
  extends SecuredController(cc, userAction) {

  def getDayReport(date: DateTime): Action[AnyContent] = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      reportService.generateDayReport(date).map { filename =>
        Ok.sendFile(
          content = new java.io.File(filename),
          fileName = _ => filename
        )
      }
    }
  })

  def getTodayReport(): Action[AnyContent] = getDayReport(DateTime.now())
}
