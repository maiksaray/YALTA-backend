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
                                  override val userAction: UserAction
                                 )(implicit ec: ExecutionContext)
  extends SecuredController(cc, userAction) {

  def getVehicles() = securedAsync(Admin.INSTANCE :: Nil, Action {
    request: Request[AnyContent] => {
      Ok("ok")
    }
  }
  )
}
