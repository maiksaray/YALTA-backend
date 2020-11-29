package controllers

import common.Serialization.{INSTANCE => Json}
import common._
import dao.{LocationDao, SessionDao}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import security.UserAction

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LocationController @Inject()(locationDao: LocationDao,
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
              locationDao.create(locationUpdate.getLat, locationUpdate.getLon, user.getId)
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

}
