package controllers

import com.google.inject.Inject
import dao.{SessionDao, UserDao}
import play.api.mvc.{AnyContent, MessagesAbstractController, MessagesControllerComponents, Request}

import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(userDao: UserDao,
                                sessionDao: SessionDao,
                                cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def index() = Action {
    Ok("Welcome to YALTA backend service! \n" +
      "Use /login with form params 'username' and 'password' to log in")
  }

  def help() = Action {
    Ok("No help for you at the moment")
  }

  def login(username: String, password: String) = Action.async {
    implicit request => {
//      TODO: add case for already exisiting session
      //    if sessionDao.getSession()
//      Future(Ok("Heyyy"))
      userDao.auth(username, password) map {
        if (_) {
          val token = sessionDao.generateToken(username)
          Ok("Success").withSession(request.session + ("sessionToken" -> token))
        }
        else {
          Ok("But no cookie").withNewSession
        }
      }
    }
  }

}
