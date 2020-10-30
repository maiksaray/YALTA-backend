package controllers

import common._
import javax.inject.Inject
import models.{VehicleRepository, _}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class VehicleController @Inject()(repo: VehicleRepository, cc: MessagesControllerComponents
                                 )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def map(vehicleClass: VehicleClass, vehicleModel: VehicleModel): VehicleModelO =
    new VehicleModelO(vehicleModel.id, vehicleModel.name, new VehicleClassO(vehicleClass.id, vehicleClass.name))

  def testKotlin() = Action{
    Ok(map(VehicleClass(0,"TESTCLASS"), VehicleModel(0,"TESTMODEL", 0)).getVehicleClass().getName())
  }

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
