package misc.camunda

import javax.inject.Inject
import play.api.Configuration
import common.Serialization.{INSTANCE => Json}


case class GroupInfo(id: String, name: String)

case class UserInfo(id: String, firstName: String, lastName: String, displayName: String)

case class GroupIdentityResponse(groups: List[GroupInfo], groupUsers: List[UserInfo])

class IdentityEndpoint @Inject()(config: Configuration) extends Endpoint(config) {

  override def resourceUrl: String = "identity"

  def groups = Map(
    common.Driver.INSTANCE -> config.get[String]("camunda.groups.driver"),
    common.Admin.INSTANCE -> config.get[String]("camunda.groups.admin")
  )

  def compareUserGroup(user: common.User): CamundaResult = {
    get(s"groups?userId=${user.getName}") match {
      case Success(result) =>
        val response = Json.fromJson(result, classOf[GroupIdentityResponse]).groups
        val userGroup = groups.get(user.getRole)
        if (response.map(_.id) contains userGroup) {
          Success("")
        } else {
          Fail
        }
      case Fail => Fail
    }
  }

}
