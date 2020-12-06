package controllers

import common.Admin
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import security.UserAction
import services.UserService

import scala.concurrent.{ExecutionContext, Future}
import common.Serialization.{INSTANCE => Json}

@Singleton
class UserController @Inject()(userService: UserService,
                               cc: MessagesControllerComponents,
                               override val userAction: UserAction
                              )(implicit ec: ExecutionContext)
  extends SecuredController(cc, userAction) {

  def getFiltered(name: String): Action[AnyContent] = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    Future.successful(NotImplemented(""))
  })

  def get(name: String): Action[AnyContent] = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    _ => {
      userService.get(name).map {
        case None => NotFound(Json.toJson(new common.BadRequest(s"User with name $name not found")))
        case Some(user) => Ok(Json.toJson(user))
      }
    }
  })

  def get(id: Int): Action[AnyContent] = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    _ => {
      userService.get(id).map {
        case None => NotFound(Json.toJson(new common.BadRequest(s"User with id $id not found")))
        case Some(user) => Ok(Json.toJson(user))
      }
    }
  })

  def create(): Action[AnyContent] = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request => {
      //      todo: extract this json processing/bad-requesting
      val body = request.body.asText
      body match {
        case Some(userData) =>
          val user = Json.fromJson(userData, classOf[common.User])
          userService.createUser(user).map {
            user => Ok(Json.toJson(user))
          }
        case None => Future.successful(BadRequest(Json.toJson(new common.BadRequest("Empty request!"))))
      }
    }
  })

  def changePass(): Action[AnyContent] = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request => {
      val body = request.body.asText
      body match {
        case Some(passData) =>
          val newPass = Json.fromJson(passData, classOf[common.ChangePassword])
          currentUser(request).flatMap {
            case Some(user) =>
              userService.changePass(user.getName, newPass.getNewPass).map {
                //                      TOdo: think of a better way than replying with user with no pass =)
                user => Ok(Json.toJson(user))
              }
            case None => Future.successful(BadRequest(Json.toJson(new common.BadRequest("No user found for session!"))))
          }
        case None => Future.successful(BadRequest(Json.toJson(new common.BadRequest("Empty request!"))))
      }
    }
  })

}
