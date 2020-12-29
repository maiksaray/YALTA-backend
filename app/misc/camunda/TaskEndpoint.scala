package misc.camunda

import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import play.api.Configuration
import common.Serialization.{INSTANCE => Json}

import scala.jdk.CollectionConverters._

case class TaskVariables(variables: java.util.Map[String, VariableValue])

case class GetTaskResponse(id: String, name: String, processInstanceId: String)

case class GetTaskReqeust(processInstanceId: String)

class TaskEndpoint @Inject()(config: Configuration) extends Endpoint(config) {
  override def resourceUrl: String = "task"

  def completeTask(id: String, variables: Map[String, VariableValue], user: common.User): CamundaResult = {

    val resolveBody = Json.toJson(TaskVariables(variables.asJava))

    post(s"$id/complete", resolveBody)(Credentials(user.getName, user.getPassword))
  }

  def getCurrentTaskId(processId: String) = {
//    val params = Map("processInstanceId" -> processId)
    val params = GetTaskReqeust(processId)
    post("", Json.toJson(params)) match {
      case Success(body) =>
        val token = TypeToken.getParameterized(classOf[java.util.List[GetTaskResponse]], classOf[GetTaskResponse]).getType
        val results: java.util.List[GetTaskResponse] = Json.fromJson(body, token)
        Success(results.get(0).id)
      case Fail => Fail
    }
  }
}
