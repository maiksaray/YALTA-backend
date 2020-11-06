package jobs
//TODO: look at how the fuck evolutions should be done cause this is fucked up
import dao.UserDao

import scala.concurrent.Future
import javax.inject._
import play.api.inject.ApplicationLifecycle

// This creates an `ApplicationStart` object once at start-up and registers hook for shut-down.
@Singleton
class ApplicationStart @Inject()(userDao:UserDao,
                                 lifecycle: ApplicationLifecycle) {
  userDao.ensureExists()
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