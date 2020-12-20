package controllers

import common.{Admin, BadRequest, Driver}
import dao.SessionDao
import play.api.mvc.{AnyContent, MessagesControllerComponents, Request}
import security.UserAction
import services.RouteService
import common.Serialization.{INSTANCE => Json}
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PointController @Inject()(routeService: RouteService,
                                sessionDao: SessionDao,
                                cc: MessagesControllerComponents,
                                override val userAction: UserAction
                               )(implicit ec: ExecutionContext)
  extends SecuredController(cc, userAction) {

  def createPoint() = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      logger.info("Received postponed location update request")
      val body = request.body.asText
      body match {
        case Some(bodyString) =>
          val point = Json.fromJson(bodyString, classOf[common.Point])
          routeService.createPoint(point).map {
            created => Ok(Json.toJson(created))
          }
        case None =>
          logger.info("Empty location update request, returning 401")
          Future.successful(BadRequest(Json.toJson(
            new BadRequest("Empty body not allowed for location update"))))
      }
    }
  })

  def changeName(pointId: Long) = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      logger.info("Received postponed location update request")
      val body = request.body.asText
      body match {
        case Some(bodyString) =>
          //          TODO: switch to something with just the name
          val update = Json.fromJson(bodyString, classOf[common.Point])
          routeService.changePointName(pointId, update.getName).map {
            updated => Ok(Json.toJson(updated))
          }
        case None =>
          logger.info("Empty location update request, returning 401")
          Future.successful(BadRequest(Json.toJson(
            new BadRequest("Empty body not allowed for location update"))))
      }
    }
  })

  def changeLocation(pointId: Long) = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      logger.info("Received postponed location update request")
      val body = request.body.asText
      body match {
        case Some(bodyString) =>
          //          TODO: switch to something with just the name
          val update = Json.fromJson(bodyString, classOf[common.Point])
          routeService.changePointLocation(pointId, update.getLat, update.getLon).map {
            updated => Ok(Json.toJson(updated))
          }
        case None =>
          logger.info("Empty location update request, returning 401")
          Future.successful(BadRequest(Json.toJson(
            new BadRequest("Empty body not allowed for location update"))))
      }
    }
  })

  def getPoint(pointId: Long) = securedAsync(Driver.INSTANCE :: Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      routeService.getPoint(pointId).map {
        case Some(point) => Ok(Json.toJson(point))
        case None => NotFound(Json.toJson(new common.BadRequest(s"Point with id $pointId not found")))
      }
    }
  })

  def getPoints() = securedAsync(Driver.INSTANCE :: Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      routeService.getPoints().map {
        points => Ok(Json.toJson(points.asJava))
      }
    }
  })

}
