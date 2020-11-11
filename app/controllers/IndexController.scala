package controllers

import com.google.inject.Inject
import common.InvalidCredentials
import dao.{SessionDao, UserDao}
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}

import scala.concurrent.ExecutionContext


class IndexController @Inject()(userDao: UserDao,
                                sessionDao: SessionDao,
                                cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc)
    with Logging {


  def index(): Action[AnyContent] = Action {
    logger.info("Serving index")
    Ok("Welcome to YALTA backend service! \n" +
      "Use /login with form params 'username' and 'password' to log in")
  }

  def help(): Action[AnyContent] = Action {
    logger.info("Someone requested help, poor soul")
    Ok("No help for you at the moment")
  }

  def login(username: String, password: String): Action[AnyContent] = Action.async {
    implicit request => {
      //      TODO: add case for already exisiting session

      userDao.auth(username, password) map {
        case Some(user) => {
          logger.info(s"user $username authed, generating session token")
          val token = sessionDao.generateToken(username)

          val userString = common.UserKt.encode(user)
          Ok(userString).withSession(request.session + ("sessionToken" -> token))
        }
        case None => {
          logger.warn(s"login attempt for username $username failed")
          val err = new InvalidCredentials("username or password are incorrect")
          Unauthorized(common.ErrorKt.encode(err))
        }
      }
    }
  }

}
