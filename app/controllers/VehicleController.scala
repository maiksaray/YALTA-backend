package controllers

import common._
import dao.{VehicleDao, VehicleRepo}
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._
import security.{UserAction, UserRequest}

import scala.concurrent.ExecutionContext

class VehicleController @Inject()(repo: VehicleRepo, cc: MessagesControllerComponents,
                                  dao: VehicleDao,
                                  val userAction: UserAction
                                 )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  //  def map(vehicleClass: VehicleClass, vehicleModel: VehicleModel): VehicleModelO =
  //    new VehicleModelO(vehicleModel.id, vehicleModel.name, new VehicleClassO(vehicleClass.id, vehicleClass.name))
  //
  def testKotlin() = Action { req =>
    Ok("ok")
  }

  //  def testKotlin() = Action{
  //    Ok(map(VehicleClass(0, "TESTCLASS"), VehicleModel(0, "TESTMODEL", 0)).getVehicleClass.getName)
  //  }


  def getVehicles = userAction { request: UserRequest[AnyContent] =>
    request.user.map(opt => opt.map(_.getRole))
    Ok("ok")
  }


  def setup = Action.async { implicit request => {
    repo.createTable()
    //    repo.create(Vehicle(None, "as", "asd", 1))
    dao.ensureExists()
    for {
      vc <- dao.createClass("test")
      svc <- dao.getVehicleClass(vc.getId)
    } yield Ok(svc.toString)
    //      repo.all().map(res =>
    //      Ok(res.toString))
  }
  }


}
