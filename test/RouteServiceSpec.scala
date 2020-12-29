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

  def userService(implicit app: Application): UserService = Application.instanceCache[UserService].apply(app)

  "Route Service" must {
    "Create point" in {
      whenReady(routeService.createPoint(30.0, 30.0, "test point")) {
        point => assert(point != null)
      }
    }

    "update point location" in {
      val newLoc = 40.0
      whenReady(routeService.createPoint(50.0, 50.0, "another test point")) {
        point =>
          whenReady(routeService.changePointLocation(point.getId, newLoc, newLoc)) {
            update =>
              assert(update.getLat == newLoc)
              assert(update.getLon == newLoc)
          }
      }
    }

    "update point name" in {
      val newName = "Not test Point"
      whenReady(routeService.createPoint(50.0, 50.0, "another test point")) {
        point =>
          whenReady(routeService.changePointName(point.getId, newName)) {
            upd => assert(upd.getName == newName)
          }
      }
    }

    "create route" in {
      whenReady(routeService.getPoint(1)) {
        p1 =>
          whenReady(routeService.getPoint(2)) {
            p2 =>
              whenReady(routeService.createRoute(3, DateTime.now(), List(p1.get, p2.get).asJava)) {
                route =>
                  assert(route != null)
              }
          }
      }
    }

    "not create second route" in {
      whenReady(routeService.getPoint(1)) {
        p1 =>
          whenReady(routeService.getPoint(2)) {
            p2 =>
              whenReady(routeService.createRoute(3, DateTime.now(), List(p1.get, p2.get).asJava).failed) {
                e =>
                  e.isInstanceOf[Exception]
              }
          }
      }
    }

    "assign route" in {
      whenReady(userService.get(1)) {
        admin =>
          whenReady(routeService.assignRoute(2, 2, admin.get)) { _ =>
            whenReady(routeService.getRoute(2)) {
              route =>
                assert(route.get.getDriverId == 2)
            }
          }
      }
    }

    "return route" in {
      whenReady(routeService.getRoute(2)) {
        route =>
          assert(route.value != null)
          assert(route.value.getId == 2)
      }
    }

    "return current route" in {
      whenReady(routeService.getCurrentRoute(2)) {
        route =>
          assert(route.value != null)
          assert(route.value.getDriverId == 2)
      }
    }

    "update point state" in {
      whenReady(routeService.updatePointState(2, 0, 3, state = true)) { _ =>
        whenReady(routeService.getRoute(2)) {
          route => assert(route.value.getPoints.get(0).getVisited)
        }
      }
    }
  }

}
