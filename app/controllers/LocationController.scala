package controllers

import common._
import dao.{LocationDao, SessionDao}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import security.{UserAction, UserRequest}
import common.Serialization.{INSTANCE => Json}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LocationController @Inject()(locationDao: LocationDao,
                                   sessionDao: SessionDao,
                                   cc: MessagesControllerComponents,
                                   override val userAction: UserAction
                                  )(implicit ec: ExecutionContext) extends SecuredController(cc, userAction) {

  def update(): Action[AnyContent] = securedAsync(Driver.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      val body = request.body.asText
      body match {
        case Some(bodyString) =>
          val locationUpdate = Json.fromJson(bodyString, classOf[common.LocationUpdate])
          currentUser(request).flatMap {
            case Some(user) =>
              locationDao.checkin(locationUpdate.getLat, locationUpdate.getLon, user.getId)
                .map(Json.toJson)
                .map(s => Ok(s))
            case None => Future.successful(
              Unauthorized(
                Json.toJson(
                  new common.Unauthorized("No user found for existing session"))))
          }
        case None => Future.successful(
          BadRequest(
            Json.toJson(
              new BadRequest("Empty body not allowed for location update"))))
      }
    }
  })

}
