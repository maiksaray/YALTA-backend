//import exceptions.InvalidDataException
//import org.scalatest.{Outcome, TestSuite}
//import org.scalatestplus.mockito.MockitoSugar
//import org.scalatestplus.play.guice.GuiceOneAppPerSuite
//import play.api.Application
//import services.UserService
//
//
////TODO: maybe switch from AsycnSpec to ScalaFutures mixin? This will change flatMaps to whenReady
//class AsyncUserServiceSpec extends AsyncPlaySpec with GuiceOneAppPerSuite with TestSuite {
//
//  def userService(implicit app: Application): UserService = Application.instanceCache[UserService].apply(app)
//
//  "UserService" must {
//    "Create user" in {
//      userService.createUser("testuser", "testpass", common.Admin.INSTANCE).flatMap {
//        user => assert(user != null)
//      }
//    }
//
//    "Throw for usernames starting with 1" in {
//      recoverToSucceededIf[InvalidDataException] {
//        userService.createUser("1", "1", common.Admin.INSTANCE)
//      }
//    }
//  }
//
//}