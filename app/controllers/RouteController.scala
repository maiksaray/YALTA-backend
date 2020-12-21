package controllers

import common.Serialization.{INSTANCE => Json}
import common.{ActionFailed, Admin, BadRequest, Driver, NotFoundError}
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
            case Some(route) =>
              logger.info(s"returning route ${route.getId} with ${route.getPoints.size()} point to user ${user.getId}")
              Ok(Json.toJson(route))
            case None =>
              logger.info(s"Could not find route for user ${user.getId}")
              NotFound(Json.toJson(new NotFoundError("There's no route for this user")))
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
              logger.info(s"maybe returning route ${route.getId} to ${user.getId}")
              user.getRole match {
                case Admin.INSTANCE => response
                case Driver.INSTANCE =>
                  if (route.getDriverId == user.getId) {
                    response
                  } else {
                    logger.warn(s"user ${user.getId} can't see route ${route.getId}")
                    Unauthorized(unauthorizedError)
                  }
              }
            case None =>
              logger.info(s"Could not find route with id ${id}")
              NotFound(Json.toJson(new NotFoundError(s"There's no route ${id}")))
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
            _ =>
              logger.info(s"successfully assigned ${routeId} to ${assign.getDriverId}")
              Ok("Assigned")
          }.recover {
            _ =>
              logger.warn(s"Could not assign ${routeId} to ${assign.getDriverId}")
              InternalServerError(Json.toJson(new ActionFailed("Can't assign")))
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
      logger.info("got request to update pointState")
      currentUser(request).flatMap {
        case Some(user) =>
          val body = request.body.asText
          body match {
            case Some(bodyString) =>
              val update = Json.fromJson(bodyString, classOf[common.UpdatePoint])
              logger.info(s"trying to update point ${pointIndex} of ${routeId} to ${update.getNewState}")
              routeService.updatePointState(routeId, pointIndex, user.getId, update.getNewState).map {
                _ =>
                  logger.info(s"successfully updated ${pointIndex} of ${routeId} to ${update.getNewState}")
                  Ok("Updated")
              }.recover {
                _ =>
                  logger.info(s"failed to update ${pointIndex} of ${routeId} to ${update.getNewState}")
                  InternalServerError(Json.toJson(new ActionFailed("Can't update point state")))
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
      logger.info("Route creation request")
      val body = request.body.asText
      body match {
        case Some(bodyString) =>
          val createRoute = Json.fromJson(bodyString, classOf[common.CreateRoute])
          logger.info(s"Trying to create route for ${createRoute.getDriverId} with ${createRoute.getPoints.size()} points")
          routeService.createRoute(createRoute.getDriverId, createRoute.getRouteDate, createRoute.getPoints).map {
            route =>
              logger.info(s"Created route ${route.getId} for ${route.getDriverId} with ${route.getPoints.size()} points")
              Ok(Json.toJson(route))
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
