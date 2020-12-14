package misc.camunda

import common.Serialization.{INSTANCE => Json}
import javax.inject.Singleton

import scala.collection.JavaConverters._

//Java Maps for gson serialization
case class CreateUserBody(profile: java.util.Map[String, String], credentials: java.util.Map[String, String])

@Singleton
class User extends Endpoint{

  override def resourceUrl: String = "user"

  def createCamundaUser(user: common.User): Unit = {
    val request = CreateUserBody(
      Map(
        "id" -> user.getName,
        "firstName" -> user.getName,
        "lastName" -> user.getName,
        "email" -> (user.getName + "@yalta.app")
      ).asJava,
      Map(
        "password" -> user.getPassword
      ).asJava
    )

    val requestBody = Json.toJson(request)

    val response = post("create", requestBody)
  }

}
