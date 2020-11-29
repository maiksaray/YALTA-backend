package controllers

import common.Role
import common.Serialization.{INSTANCE => Json}
import javax.inject.Inject
import play.api.Logging
import play.api.mvc._
import security.{UserAction, UserRequest}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class SecuredController @Inject()(cc: ControllerComponents,
                                  val userAction: UserAction
                                 )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with Logging {

  val unauthorizedError: String = Json.toJson(
    new common.Unauthorized("Insufficient rights to access requested resource"))

  def currentUser(request: Request[AnyContent]): Future[Option[common.User]] = {
    request.asInstanceOf[UserRequest[AnyContent]].user
  }

  def securedAsync[AnyContent](roles: Seq[Role],
                               actionParam: Action[AnyContent]): Action[AnyContent] =
    userAction.async(actionParam.parser) {
      userRequest: UserRequest[AnyContent] =>

        val user = Await.result(userRequest.user, Duration.Inf)
        val role = user match {
          case None => None
          case Some(value) => value.getRole
        }
        //        Todo: propagate this in a more elegant way
        role match {
          case None =>
            logger.warn("Returning 401 for request with no session provided")
            Future(Unauthorized(unauthorizedError))
          case _ => if (roles.contains(role)) {
            actionParam(userRequest)
          } else {
            logger.warn(s"User with role $role tried to request resource with permissions: $roles ${userRequest.path}")
            Future(Unauthorized(unauthorizedError))
          }
        }
    }
}
