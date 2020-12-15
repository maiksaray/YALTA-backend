package services

import javax.inject.{Inject, Singleton}
import misc.camunda.{TaskEndpoint, UserEndpoint}
import play.api.Configuration

@Singleton
class CamundaService @Inject()(val config: Configuration, val user: UserEndpoint, val task: TaskEndpoint) {

  def enabled: Boolean = config.get[Boolean]("camunda.enabled")
}
