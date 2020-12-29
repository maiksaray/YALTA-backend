package misc.camunda

import common.Serialization.{INSTANCE => Json}
import common.User
import javax.inject.{Inject, Singleton}
import play.api.Configuration

case class UserProfile(id: String, firstName: String, lastName: String, email: String)

case class UserCredentials(password: String)

case class UpdateUserCredentials(password: String, authenticatedUserPassword: String)

case class CreateUser(profile: UserProfile, credentials: UserCredentials)


@Singleton
class UserEndpoint @Inject()(config: Configuration,
                             group: GroupEndpoint,
                             identity: IdentityEndpoint) extends Endpoint(config) {

  override def resourceUrl: String = "user"

  private def profile(user: User) =
    UserProfile(
      user.getName,
      user.getName,
      user.getName,
      (user.getName + "@yalta.app")
    )

  private def credentials(user: User) =
    UserCredentials(
      user.getPassword
    )

  def syncCamundaUser(user: User): CamundaResult = {
    if (userExists(user)) {
      updateCamundaUser(user)
      identity.compareUserGroup(user) match {
        case Fail => group.addUserToGroup(user)
        case anything => anything
      }
    } else {
      createCamundaUser(user) match {
        case Success(_) => group.addUserToGroup(user)
        case Fail => Fail
      }
    }
  }

  def updateCamundaUser(user: User): CamundaResult = {
    val profileBody = Json.toJson(profile(user))
    post(s"${user.getName}/profile", profileBody) match {
      case Fail => Fail
      case _ =>
        val credBody = Json.toJson(UpdateUserCredentials(user.getPassword, defaultCreds.password))
        post(s"${user.getName}/credentials", credBody)
    }
  }

  def userExists(user: User): Boolean = {
    val result = get(s"${user.getName}/profile")
    result.isInstanceOf[Success]
  }

  def createCamundaUser(user: common.User): CamundaResult = {
    val request = CreateUser(
      profile(user),
      credentials(user)
    )

    val requestBody = Json.toJson(request)

    post("create", requestBody)
  }
}
