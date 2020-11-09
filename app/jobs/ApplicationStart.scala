package jobs
import dao.UserDao
import common.{Admin, Driver}

import scala.concurrent.Future
import javax.inject._
import play.api.inject.ApplicationLifecycle

@Singleton
class ApplicationStart @Inject()(userDao:UserDao,
                                 lifecycle: ApplicationLifecycle) {

  //TODO: look at how the fuck evolutions should be done cause this is fucked up
  userDao.ensureExists()
  userDao.create("admin", "admin", Admin.INSTANCE)
  userDao.create("driver", "driver", Driver.INSTANCE)
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