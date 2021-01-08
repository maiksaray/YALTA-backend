package controllers

import common.Role
import common.Serialization.{INSTANCE => Json}
import javax.inject.Inject
import play.api.Logging
import play.api.mvc._
import security.{UserAction, UserRequest}

import scala.concurrent.{ExecutionContext, Future}

class SecuredController @Inject()(cc: ControllerComponents,
                                  val userAction: UserAction
                                 )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with Logging {

  val unauthorizedError: String = Json.toJson(
    new common.Unauthorized("Insufficient rights to access requested resource"))

  def currentUser(request: Request[AnyContent]): Future[Option[common.User]] =
    request match {
      case userRequest: UserRequest[AnyContent] => userRequest.user
      case _ =>
        logger.error(s"Invalid request type detected in controller: ${request.getClass}")
        Future.successful(None)
    }

  def securedAsync[AnyContent](roles: Seq[Role],
                               actionParam: Action[AnyContent]): Action[AnyContent] =
    userAction.async(actionParam.parser) {
      userRequest: UserRequest[AnyContent] =>
        userRequest.user.flatMap { user =>
          user.map(_.getRole) match {
            case None =>
              logger.warn("Returning 401 for request with no session provided")
              Future(Unauthorized(unauthorizedError))
            case Some(role) => if (roles.contains(role)) {

              actionParam(userRequest)

            } else {
              logger.warn(s"User with role $role tried to request resource with permissions: $roles ${userRequest.path}")
              Future(Unauthorized(unauthorizedError))
            }
          }
        }
    }
}
