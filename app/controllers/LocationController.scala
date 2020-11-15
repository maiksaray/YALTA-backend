package controllers

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
                                  )(implicit ec: ExecutionContext) extends SecuredController(cc, userAction) {

  def checkin(): Action[AnyContent] = securedAsync(Driver.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      val body = request.body.asText
      body match {
        case Some(bodyString) =>
//          val location = LocationKt.decode(bodyString)
//          locationDao.checkin(location).map {
//            l =>
//              Ok(LocationKt.encode(l))
//          }
          Future.successful(Ok(""))
        case None => Future.successful(BadRequest(""))//ErrorKt.encode(new BadRequest("Empty request!"))))
      }
    }
  })

}
