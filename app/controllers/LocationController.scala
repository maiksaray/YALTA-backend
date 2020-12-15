package controllers

import java.sql.Timestamp

import common.Serialization.{INSTANCE => Json}
import common._
import dao.SessionDao
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import security.UserAction
import services.LocationService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LocationController @Inject()(locationService: LocationService,
                                   sessionDao: SessionDao,
                                   cc: MessagesControllerComponents,
                                   override val userAction: UserAction
                                  )(implicit ec: ExecutionContext)
  extends SecuredController(cc, userAction) {

  def update(): Action[AnyContent] = securedAsync(Driver.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      logger.info("Received location update request")
      val body = request.body.asText
      body match {
        case Some(bodyString) =>
          logger.info(bodyString)
          val locationUpdate = Json.fromJson(bodyString, classOf[common.LocationUpdate])
          currentUser(request).flatMap {
            case Some(user) =>
              logger.info(s"Updating location for user ${user.getName}(${user.getId})")
              locationService.create(locationUpdate.getLat, locationUpdate.getLon, user.getId)
                .map { location =>
                  logger.info(s"Added location record for ${user.getName} at ${location.getTimestamp}")
                  Json.toJson(location)
                }.map {
                json => Ok(json)
              }
            case None =>
              logger.error(s"User session was verified, but now no user found, THIS SHOULD NEVER HAPPEN")
              Future.successful(InternalServerError(Json.toJson(
                new common.InternalServerError("No user found for existing session, this should never happen"))))
          }
        case None =>
          logger.info("Empty location update request, returning 401")
          Future.successful(BadRequest(Json.toJson(
            new BadRequest("Empty body not allowed for location update"))))
      }
    }
  })

  def postHistory(): Action[AnyContent] = securedAsync(Driver.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] =>
      logger.info("Received postponed location update request")
      val body = request.body.asText
      body match {
        case Some(bodyString) =>
          logger.info(bodyString)
          val locations = Json.fromJson(bodyString, classOf[List[common.OffsetedLocationUpdate]])
          currentUser(request).flatMap {
            case Some(user) =>
              val updated = locationService.postOfflineHistory(locations, user.getId)
              updated.map {
                res => Ok(res.toString)
              }
            case None =>
              logger.error(s"User session was verified, but now no user found, THIS SHOULD NEVER HAPPEN")
              Future.successful(InternalServerError(Json.toJson(
                new common.InternalServerError("No user found for existing session, this should never happen"))))
          }
        case None =>
          logger.info("Empty location update request, returning 401")
          Future.successful(BadRequest(Json.toJson(
            new BadRequest("Empty body not allowed for location update"))))
      }
  })

  def getOwnHistory(from: Timestamp, to: Timestamp): Action[AnyContent] = securedAsync(Driver.INSTANCE :: Nil, Action.async {
    request => {
      currentUser(request).flatMap {
        case Some(user) =>
          logger.info(s"Location istory for user ${user.getId}|${user.getName} by himself from ${from} to ${to}")
          val history = locationService.getHistory(user.getId, from, to)
          history.map {
            locations =>
              logger.info(s"Returning location history for user ${user.getId}|${user.getName} (${locations.length})")
              Ok(Json.toJson(locations))
          }
        case None =>
          logger.error(s"User session was verified, but now no user found, THIS SHOULD NEVER HAPPEN")
          Future.successful(InternalServerError(Json.toJson(
            new common.InternalServerError("No user found for existing session, this should never happen"))))
      }
    }
  })

  def getHistory(id: Long, from: Timestamp, to: Timestamp): Action[AnyContent] = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request =>
      logger.info(s"Location istory for user ${id} by admin from ${from} to ${to}")
      val history = locationService.getHistory(id, from, to)
      history.map {
        locations =>
          logger.info(s"Returning location history for user ${id} (${locations.length})")
          Ok(Json.toJson(locations))
      }
  })

}
