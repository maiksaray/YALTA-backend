package misc.camunda

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import common.Serialization.{INSTANCE => Json}

case class VariableValue(value: String, `type`: String)

case class StartProcessRequest(variables: Map[String, VariableValue], businessKey: String)

case class LinksItem(method: String, href: String, rel: String)

case class StartProcessResponse(links: java.util.List[LinksItem],
                                id: String,
                                definitionId: String,
                                businessKey: String,
                                caseInstanceId: String, ended: Boolean, suspended: Boolean, tenantId: Object)

@Singleton
class ProcessEndpoint @Inject()(config: Configuration) extends Endpoint(config) {
  override def resourceUrl: String = "process-definition"

  private val processDefinition = config.get[String]("camunda.routeProcess")

  def startRouteProcess(id: Long): CamundaResult = {
    val urlPart = s"$processDefinition/start"
    val body = StartProcessRequest(
      Map(
        "routeId" -> VariableValue(id.toString, "String")
      ),
      s"RouteId${id}")

    post(urlPart, Json.toJson(body)) match {
      case Success(body) =>
        val response = Json.fromJson(body, classOf[StartProcessResponse])
        Success(response.id)
      case Fail => Fail
    }
  }
}
