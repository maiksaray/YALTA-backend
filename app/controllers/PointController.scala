package controllers

import common.Serialization.{INSTANCE => Json}
import common.{Admin, BadRequest, Driver}
import dao.SessionDao
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AnyContent, MessagesControllerComponents, Request}
import security.UserAction
import services.RouteService

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Singleton
class PointController @Inject()(routeService: RouteService,
                                sessionDao: SessionDao,
                                cc: MessagesControllerComponents,
                                override val userAction: UserAction
                               )(implicit ec: ExecutionContext)
  extends SecuredController(cc, userAction) {

  def createPoint() = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      logger.info("Received point creation request")
      val body = request.body.asText
      body match {
        case Some(bodyString) =>
          val point = Json.fromJson(bodyString, classOf[common.Point])
          logger.info(s"Trying to create point ${point.getName}")
          routeService.createPoint(point).map {
            created =>
              logger.info(s"Point ${point.getName} created")
              Ok(Json.toJson(created))
          }
        case None =>
          logger.info("Empty point creation request, returning 401")
          Future.successful(BadRequest(Json.toJson(
            new BadRequest("Empty body not allowed"))))
      }
    }
  })

  def changeName(pointId: Long) = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      logger.info("Received point name change request")
      val body = request.body.asText
      body match {
        case Some(bodyString) =>
          //          TODO: switch to something with just the name
          val update = Json.fromJson(bodyString, classOf[common.Point])
          logger.info(s"Trying to change point ${update.getId} to ${update.getName}")
          routeService.changePointName(pointId, update.getName).map {
            updated =>
              logger.info(s"Point ${updated.getId} name changed to ${update.getName}")
              Ok(Json.toJson(updated))
          }
        case None =>
          logger.info("Empty point name update request, returning 401")
          Future.successful(BadRequest(Json.toJson(
            new BadRequest("Empty body not allowed"))))
      }
    }
  })

  def changeLocation(pointId: Long) = securedAsync(Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      logger.info("Received point location change request")
      val body = request.body.asText
      body match {
        case Some(bodyString) =>
          //          TODO: switch to something with just the location?
          val update = Json.fromJson(bodyString, classOf[common.Point])
          logger.info(s"Trying to change point ${update.getId} to ${update.getLat}, ${update.getLon}")
          routeService.changePointLocation(pointId, update.getLat, update.getLon).map {
            updated =>
              logger.info(s"Changed point ${update.getId} to ${update.getLat}, ${update.getLon}")
              Ok(Json.toJson(updated))
          }
        case None =>
          logger.info("Empty point location update request, returning 401")
          Future.successful(BadRequest(Json.toJson(
            new BadRequest("Empty body not allowed"))))
      }
    }
  })

  def getPoint(pointId: Long) = securedAsync(Driver.INSTANCE :: Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      routeService.getPoint(pointId).map {
        case Some(point) =>
          logger.info(s"returning point ${point.getId}")
          Ok(Json.toJson(point))
        case None =>
          logger.info(s"Point ${pointId} not found")
          NotFound(Json.toJson(new common.BadRequest(s"Point with id $pointId not found")))
      }
    }
  })

  def getPoints() = securedAsync(Driver.INSTANCE :: Admin.INSTANCE :: Nil, Action.async {
    request: Request[AnyContent] => {
      routeService.getPoints().map {
        points =>
          logger.info(s"returning all points")
          Ok(Json.toJson(points.asJava))
      }
    }
  })

}
