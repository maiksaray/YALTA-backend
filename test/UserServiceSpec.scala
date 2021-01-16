import misc.InvalidDataException
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Outcome, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import services.UserService

class UserServiceSpec extends PlaySpec with GuiceOneAppPerSuite with TestSuite with ScalaFutures {

  def userService(implicit app: Application): UserService = Application.instanceCache[UserService].apply(app)

  "UserService" must {
    "Create user" in {
      val f = userService.createUser("testuser", "testpass", common.Admin.INSTANCE)
      whenReady(f) {
        user => assert(user != null)
      }
    }

    "Throw for usernames starting with digit" in {
      assertThrows[InvalidDataException] {
        userService.createUser("1", "1", common.Admin.INSTANCE)
      }
    }
  }
}
