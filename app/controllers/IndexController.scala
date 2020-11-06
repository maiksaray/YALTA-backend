package controllers

import com.google.inject.Inject
import dao.{SessionDao, UserDao}
import play.api.mvc.{AnyContent, MessagesAbstractController, MessagesControllerComponents, Request}

import scala.concurrent.ExecutionContext

class IndexController @Inject()(userDao: UserDao,
                                sessionDao: SessionDao,
                                cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def index() = Action {
    Ok("Welcome to YALTA backend service! \n" +
      "Use /login with form params 'login' and 'password' to log in")
  }

  def help() = Action {
    Ok("No help for you at the moment")
  }

  def login(username: String, password: String) = Action.async {
    implicit request => {
      val user = new common.User(null, username, password, null)
//      TODO: add case for already exisiting session
      //    if sessionDao.getSession()
      userDao.auth(user) map {
        if (_) {
          val token = sessionDao.generateToken(user)
          //      Redirect(index()).withSession(request.session + ("sessionToken" -> token))
          Ok("").withSession(request.session + ("sessionToken" -> token))
        }
        else {
          Unauthorized("Invalid password").withNewSession
        }
      }
    }
  }

}
