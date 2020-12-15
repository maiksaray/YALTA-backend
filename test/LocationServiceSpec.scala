import org.joda.time.DateTime
import org.scalatest.TestSuite
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import services.LocationService

class LocationServiceSpec extends PlaySpec with GuiceOneAppPerSuite with TestSuite with ScalaFutures {
  //TODO: make tests non-dependent on each other =)

  def locationService(implicit app: Application) = Application.instanceCache[LocationService].apply(app)

  val historyStart = DateTime.now()

  "Location Serivce" must {
    "Create new locations" in {
      val newLoc = locationService.create(10.0, 10.0, 1)
      whenReady(newLoc) {
        loc => assert(newLoc != null)
      }
    }

    "Post History" in {
      Thread.sleep(1000)
      val updates = new common.OffsetedLocationUpdate(10.0, 10.10, 0.3) ::
        new common.OffsetedLocationUpdate(11.0, 11.10, 0.200) ::
        new common.OffsetedLocationUpdate(12.0, 12.10, 0.300) ::
        Nil
      val update = locationService.postOfflineHistory(updates, 1)
      whenReady(update) {
        case Some(count) => assert(count == updates.length)
        case None => fail("insertion failed")
      }
    }

    "Get gistory" in {
      val historyEnd = DateTime.now()
      val histpry = locationService.getHistory(1, historyStart, historyEnd)
      whenReady(histpry) {
        res => assert(res.length == 4)
      }
    }
  }

}
