package controllers

import javax.inject.Inject
import models.VehicleRepository
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext
import javax.inject._

import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class VehicleController @Inject()(repo: VehicleRepository,cc: MessagesControllerComponents
                                 )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def getVehicles = Action.async { implicit request =>
    repo.getClasses().map { classes =>
      Ok(Json.toJson(classes))
    }
  }



  def setup = Action {
    repo.fillStub()
    Ok("filled")
  }


}
