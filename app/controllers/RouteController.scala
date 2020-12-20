package controllers

import common.{Admin, BadRequest, Driver}
import dao.SessionDao
import javax.inject.Inject
import play.api.mvc.{AnyContent, MessagesControllerComponents, Request}
import security.UserAction
import services.{LocationService, RouteService}
import common.Serialization.{INSTANCE => Json}

import scala.concurrent.{ExecutionContext, Future}

class RouteController @Inject()(routeService: RouteService,
                                sessionDao: SessionDao,
                                cc: MessagesControllerComponents,
                                override val userAction: UserAction
                               )(implicit ec: ExecutionContext)
  extends SecuredController(cc, userAction) {

  def getCurrentRoute() = securedAsync(Driver.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      currentUser(request).flatMap {
        case Some(user) =>
          logger.info(s"L")
          routeService.getCurrentRoute(user.getId).map {
            route => Ok(Json.toJson(route))
          }
        case None =>
          logger.error(s"User session was verified, but now no user found, THIS SHOULD NEVER HAPPEN")
          Future.successful(InternalServerError(Json.toJson(
            new common.InternalServerError("No user found for existing session, this should never happen"))))
      }
    }
  })

  def assignRoute(routeId: Long, driverId: Long) = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      routeService.assignRoute(routeId, driverId).map {
        res => Ok("")
      }
    }
  })

  def updatePointState(routeId: Long, routePointId: Long) = securedAsync(Driver.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      currentUser(request).flatMap {
        case Some(user) =>
          routeService.updatePointState(routeId, routePointId, user.getId).map{
            res => Ok("")
          }
        case None =>
          logger.error(s"User session was verified, but now no user found, THIS SHOULD NEVER HAPPEN")
          Future.successful(InternalServerError(Json.toJson(
            new common.InternalServerError("No user found for existing session, this should never happen"))))
      }
    }
  })

  def createRoute() = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      logger.info("Received postponed location update request")
      val body = request.body.asText
      body match {
        case Some(bodyString) =>
          Future.successful(Ok(""))
        case None =>
          logger.info("Empty location update request, returning 401")
          Future.successful(BadRequest(Json.toJson(
            new BadRequest("Empty body not allowed for location update"))))
      }
    }
  })

  def addPoints(routeId:Long) = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      logger.info("Received postponed location update request")
      val body = request.body.asText
      body match {
        case Some(bodyString) =>
          Future.successful(Ok(""))
        case None =>
          logger.info("Empty location update request, returning 401")
          Future.successful(BadRequest(Json.toJson(
            new BadRequest("Empty body not allowed for location update"))))
      }
    }
  })

}
