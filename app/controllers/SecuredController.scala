package controllers

import common.{Admin, Role}
import dao.{VehicleDao, VehicleRepo}
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents, Request, Result}
import security.{UserAction, UserRequest}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class SecuredController @Inject()(cc: MessagesControllerComponents,
                                  val userAction: UserAction
                                 )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def securedAsync[AnyContent](roles: Seq[Role],
                               actionParam: Action[AnyContent]):Action[AnyContent] =
    userAction.async(actionParam.parser) {
      userRequest: UserRequest[AnyContent] =>
      //      userRequest.user.map {
      //        case Some(user) =>
      //          if (roles.contains(user.getRole)) {
      val user = Await.result(userRequest.user, Duration.Inf)
      val role = user match {
        case None => None
        case Some(value) => value.getRole
      }
      role match {
        case None => Future(Unauthorized)
        case _ => if(roles.contains(role)){
          actionParam (userRequest)
        }else{
          Future(Unauthorized)
        }
      }
      //          } else {
      //            Unauthorized
      //          }
      //        case None => Unauthorized
      //      }
    }


  def sec(roles: Seq[Role], block: => Result) = userAction(block)

  def test() = securedAsync(Admin.INSTANCE :: Nil, Action{
    request: Request[AnyContent] => {
      Ok("Only Admin")
    }
  }
  )

}
