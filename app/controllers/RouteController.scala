package controllers

import common.Serialization.{INSTANCE => Json}
import common.{Admin, BadRequest, Driver}
import dao.SessionDao
import javax.inject.Inject
import play.api.mvc.{AnyContent, MessagesControllerComponents, Request}
import security.UserAction
import services.RouteService

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
          logger.info(s"returning current route for user ${user.getId}")
          routeService.getCurrentRoute(user.getId).map {
            case Some(route) => Ok(Json.toJson(route))
            case None => NotFound("")
          }
        case None =>
          logger.error(s"User session was verified, but now no user found, THIS SHOULD NEVER HAPPEN")
          Future.successful(InternalServerError(Json.toJson(
            new common.InternalServerError("No user found for existing session, this should never happen"))))
      }
    }
  })

  def getRoute(id: Long) = securedAsync(Driver.INSTANCE :: Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      currentUser(request).flatMap {
        case Some(user) =>
          logger.info(s"returning route with id $id")
          routeService.getRoute(id).map {
            case Some(route) =>
              val response = Ok(Json.toJson(route))
              user.getRole match {
                case Admin.INSTANCE => response
                case Driver.INSTANCE =>
                  if (route.getDriverId == user.getId) {
                    response
                  } else {
                    Unauthorized(unauthorizedError)
                  }
              }
            case None => NotFound("")
          }

        case None =>
          logger.error(s"User session was verified, but now no user found, THIS SHOULD NEVER HAPPEN")
          Future.successful(InternalServerError(Json.toJson(
            new common.InternalServerError("No user found for existing session, this should never happen"))))
      }
    }
  })

  def assignRoute(routeId: Long) = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      logger.info("Driver assign request")
      val body = request.body.asText
      body match {
        case Some(bodyString) =>
          val assign = Json.fromJson(bodyString, classOf[common.AssignDriver])
          logger.info(s"trying to assign $routeId to ${assign.getDriverId}")
          routeService.assignRoute(routeId, assign.getDriverId).map {
            _ => Ok("Assigned")
          }.recover {
            _ => InternalServerError("Assign failed/TODO: add special message =)")
          }
        case None =>
          logger.info("Empty location update request, returning 401")
          Future.successful(BadRequest(Json.toJson(
            new BadRequest("Empty body not allowed for location update"))))
      }
    }
  })

  def updatePointState(routeId: Long, pointIndex: Int) = securedAsync(Driver.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      currentUser(request).flatMap {
        case Some(user) =>
          val body = request.body.asText
          body match {
            case Some(bodyString) =>
              val update = Json.fromJson(bodyString, classOf[common.UpdatePoint])
              routeService.updatePointState(routeId, pointIndex, user.getId, update.getNewState).map {
                _ => Ok("UPdated")
              }.recover {
                _ => InternalServerError("Update Failed/TODO: add special message =)")
              }
            case None => Future.successful(BadRequest(Json.toJson(new common.BadRequest(""))))
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
          val createRoute = Json.fromJson(bodyString, classOf[common.CreateRoute])
          routeService.createRoute(createRoute.getDriverId, createRoute.getRouteDate, createRoute.getPoints).map {
            route => Ok(Json.toJson(route))
          }
        case None =>
          logger.info("Empty location update request, returning 401")
          Future.successful(BadRequest(Json.toJson(
            new BadRequest("Empty body not allowed for location update"))))
      }
    }
  })

  def addPoints(routeId: Long) = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      logger.info("Received postponed location update request")
      val body = request.body.asText
      body match {
        case Some(bodyString) =>
          Future.successful(NotImplemented(""))
        case None =>
          logger.info("Empty location update request, returning 401")
          Future.successful(BadRequest(Json.toJson(
            new BadRequest("Empty body not allowed for location update"))))
      }
    }
  })

}
