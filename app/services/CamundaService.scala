package services

import javax.inject.{Inject, Singleton}
import misc.camunda.{CamundaResult, Fail, GroupEndpoint, ProcessEndpoint, Success, TaskEndpoint, TaskVariables, UserEndpoint, VariableValue}
import play.api.Configuration

import scala.collection.mutable

@Singleton
class CamundaService @Inject()(val config: Configuration,
                               val user: UserEndpoint,
                               val group: GroupEndpoint,
                               val task: TaskEndpoint,
                               val process: ProcessEndpoint) {

  def enabled: Boolean = config.get[Boolean]("camunda.enabled")

  private var processes = mutable.Map[Long, String]()

  def startProcess(routeId: Long): CamundaResult = {
    process.startRouteProcess(routeId) match {
      case Success(result) =>
        processes.addOne((routeId, result))
        Success(result)
      case _ => Fail
    }
  }

  def assignRoute(routeId: Long, driver: common.User, admin: common.User): CamundaResult = {
    processes.get(routeId) match {
      case Some(id) =>
        task.getCurrentTaskId(id) match {
          case Success(taskId) =>
            val vars = Map(
              "driverId" -> VariableValue(driver.getName, "String")
            )
            task.completeTask(taskId, vars, admin)
          case Fail => Fail
        }
      case None =>
        startProcess(routeId) match {
          case Success(_) => assignRoute(routeId, driver, admin)
          case Fail => Fail
        }
    }
  }

  def completeRoute(routeId: Long, driver: common.User): CamundaResult = {
    processes.get(routeId) match {
      case Some(id) =>
        task.getCurrentTaskId(id) match {
          case Success(taskId) =>
            val vars = Map(
              "driverId" -> VariableValue(driver.getName, "String")
            )
            task.completeTask(taskId, vars, driver)
          case Fail => Fail
        }
      case None => Fail
    }
  }
}
