package misc.camunda

import javax.inject.Inject
import play.api.Configuration
import common.Serialization.{INSTANCE => Json}
import scala.jdk.CollectionConverters._

case class TaskVariables(variables: java.util.Map[String, Object])

class TaskEndpoint @Inject()(config: Configuration) extends Endpoint(config) {
  override def resourceUrl: String = "task"

  def resolveTask(id: String, variables: Map[String, Object], user: common.User): CamundaResult = {
    val resolveBody = Json.toJson(TaskVariables(variables.asJava))

    post(s"$id/resolve", resolveBody)(Credentials(user.getName, user.getPassword))
  }
}
