package controllers

import common._
import common.Serialization.{INSTANCE => Json}
import dao.{SessionDao, UserDao}
import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import security.UserAction

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject()(userDao: UserDao,
                                sessionDao: SessionDao,
                                cc: MessagesControllerComponents,
                                override val userAction: UserAction
                               )(implicit ec: ExecutionContext)
  extends SecuredController(cc, userAction)
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

  val sessionTokenName = "sessionToken"

  def login(username: String, password: String): Action[AnyContent] = Action.async {
    implicit request => {
      //      TODO: add case for already exisiting session

      userDao.auth(username, password) map {
        case Some(user) => {
          logger.info(s"user $username authed, generating session token")
          val token = sessionDao.generateToken(username)

          val userString = Json.toJson(user)
          Ok(userString).withSession(request.session + (sessionTokenName -> token))
        }
        case None => {
          logger.warn(s"login attempt for username $username failed")
          val err = new InvalidCredentials("username or password are incorrect")
          Unauthorized(Json.toJson(err))
        }
      }
    }
  }

  def whoami(): Action[AnyContent] = Action.async {
    implicit request => {
      val maybeToken = request.session
        .get(sessionTokenName)
      maybeToken match {
        case None => Future.successful(Unauthorized(unauthorizedError))
        case Some(token) => {
          val maybeSession = sessionDao.getSession(token)
          maybeSession match {
            case None => Future.successful(Unauthorized(unauthorizedError))
            case Some(session) => {
              userDao.getUser(session.username).map {
                case None => Unauthorized(unauthorizedError)
                case Some(user) => Ok(Json.toJson(user))
              }
            }
          }
        }
      }
    }
  }

  def logout(): Action[AnyContent] = Action {
    implicit request => {
      val maybeToken = request.session
        .get(sessionTokenName)
      maybeToken match {
        case None => Unauthorized(unauthorizedError)
        case Some(token) =>
          val maybeDeleted = sessionDao.deleteSession(token)
          maybeDeleted match {
            case None => BadRequest(Json.toJson(
              new BadRequest("Can not terminate absent session")))
            case Some(_) => Ok("Successfully logged out")
          }
      }
    }
  }

}
