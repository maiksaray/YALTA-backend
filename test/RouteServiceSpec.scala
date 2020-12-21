import org.joda.time.DateTime
import org.scalatest.TestSuite
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import services.{RouteService, UserService}
import scala.jdk.CollectionConverters._

class RouteServiceSpec extends PlaySpec with GuiceOneAppPerSuite with TestSuite with ScalaFutures {

  def routeService(implicit app: Application): RouteService = Application.instanceCache[RouteService].apply(app)

  "Route Service" must {
    "Create point" in {
      val f = routeService.createPoint(30.0, 30.0, "test point")
      whenReady(f) {
        point => assert(point != null)
      }
    }

    "update point location" in {
      val f = routeService.createPoint(50.0, 50.0, "another test point")
      val newLoc = 40.0
      whenReady(f) {
        p =>
          val change = routeService.changePointLocation(p.getId, newLoc, newLoc)
          whenReady(change) {
            p =>
              assert(p.getLat == newLoc)
              assert(p.getLon == newLoc)
          }
      }
    }

    "update point name" in {
      val f = routeService.createPoint(50.0, 50.0, "another test point")
      val newName = "Not test Point"
      whenReady(f) {
        p =>
          val change = routeService.changePointName(p.getId, newName)
          whenReady(change) {
            p => assert(p.getName == newName)
          }
      }
    }

    "create route" in {
      val f1 = routeService.getPoint(1)
      whenReady(f1) {
        p1 =>
          val f2 = routeService.getPoint(2)
          whenReady(f2) {
            p2 =>
              val f = routeService.createRoute(3, DateTime.now(), List(p1.get, p2.get).asJava)
              whenReady(f) {
                r =>
                  assert(r != null)
              }
          }
      }

    }

    "not create second route" in {
      val f1 = routeService.getPoint(1)
      whenReady(f1) {
        p1 =>
          val f2 = routeService.getPoint(2)
          whenReady(f2) {
            p2 =>
              whenReady(routeService.createRoute(3, DateTime.now(), List(p1.get, p2.get).asJava).failed) {
                e =>
                  e.isInstanceOf[Exception]
              }
          }
      }
    }

    "assign route" in {
      whenReady(routeService.assignRoute(2, 2)) { _ =>
        whenReady(routeService.getRoute(2)) {
          r =>
            assert(r.get.getDriverId == 2)
        }
      }
    }

    "return route" in {
      whenReady(routeService.getRoute(2)) {
        r =>
          assert(r.value != null)
          assert(r.value.getId == 2)
      }
    }

    "return current route" in {
      whenReady(routeService.getCurrentRoute(3)) {
        r =>
          assert(r.value != null)
          assert(r.value.getDriverId == 3)
      }
    }

    "update point state" in {
      whenReady(routeService.updatePointState(2, 0, 3, state = true)) { _ =>
        whenReady(routeService.getRoute(2)) {
          r => assert(r.value.getPoints.get(0).getVisited)
        }
      }
    }
  }

}
