package controllers

import common.Admin
import dao.SessionDao
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import security.UserAction
import services.{ReportService, RouteService}

import scala.concurrent.ExecutionContext

class ReportController @Inject()(routeService: RouteService,
                                 sessionDao: SessionDao,
                                 reportService: ReportService,
                                 cc: MessagesControllerComponents,
                                 override val userAction: UserAction
                                )(implicit ec: ExecutionContext)
  extends SecuredController(cc, userAction) {

  def getDayReport(date: DateTime): Action[AnyContent] = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      logger.info(s"Got request for Route day report for $date")
      reportService.generateDayReport(date).map { filename =>
        logger.info(s"Serving report file $filename")
        Ok.sendFile(
          content = new java.io.File(filename),
          fileName = _ => filename
        )
      }
    }
  })

  def getTodayReport(): Action[AnyContent] = getDayReport(DateTime.now())

  def getMapReport(id:Long, from: DateTime, to: DateTime) = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      logger.info(s"Got request for Route day report for $from")
      reportService.getMapPic(from, id).map { filename =>
        logger.info(s"Serving report file $filename")
        Ok.sendFile(
          content = new java.io.File(filename),
          fileName = _ => filename
        )
      }
    }
  })

}
