package misc.camunda

import javax.inject.Inject
import play.api.Configuration

class GroupEndpoint @Inject()(config: Configuration) extends Endpoint(config) {

  override def resourceUrl: String = "group"

  def groups = Map(
    common.Driver.INSTANCE -> config.get[String]("camunda.groups.driver"),
    common.Admin.INSTANCE -> config.get[String]("camunda.groups.admin")
  )

  def addUserToGroup(user: common.User): CamundaResult = {
    val url = s"${groups(user.getRole)}/members/${user.getName}"
    put(url, "")
  }
}
