package misc

import dao.{LocationDao, RouteDao, UserDao}
import common.{Admin, Driver}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import javax.inject._
import play.api.Logging
import play.api.inject.ApplicationLifecycle

@Singleton
class ApplicationStart @Inject()(userDao: UserDao,
                                 locationDao: LocationDao,
                                 routeDao: RouteDao,
                                 lifecycle: ApplicationLifecycle) extends Logging {

  logger.info("Starting application, preparing db")
  //TODO: look at how the fuck evolutions should be done cause this is fucked up
  //  ALso, here we go around services layer, because we can
  Await.result(userDao.ensureExists(), 5 seconds)
  logger.info("Ensured db is in place and default users exist")

  Await.result(locationDao.ensureExists(), 5 seconds)
  logger.info("Inited location storage")

  Await.result(routeDao.ensureExists(), 5 seconds)
  logger.info("Inited route storage")

  // Shut-down hook
  lifecycle.addStopHook { () =>
    Future.successful(())
  }
  //...
}

import com.google.inject.AbstractModule

class OnStartupModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ApplicationStart]).asEagerSingleton()
  }
}