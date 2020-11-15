package controllers

import common._
import dao.VehicleDao
import javax.inject.Inject
import play.api.mvc._
import security.UserAction

import scala.concurrent.ExecutionContext

class VehicleController @Inject()(dao: VehicleDao,
                                  cc: MessagesControllerComponents,
                                  override val userAction: UserAction
                                 )(implicit ec: ExecutionContext)
  extends SecuredController(cc, userAction) {

  def getVehicles(): Action[AnyContent] = securedAsync(Admin.INSTANCE :: Nil, Action {
    request: Request[AnyContent] => {
      Ok("{}")
    }
  }
  )
}
