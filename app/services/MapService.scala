package services

import java.io.File

import common.Location
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.Logging
import sttp.client3.{HttpURLConnectionBackend, NoBody}
import sttp.client3.quick.{basicRequest, _}
import sttp.model.Uri

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MapService @Inject()(locationService: LocationService)(implicit ec: ExecutionContext) extends Logging {

  def baseUrl = s"https://static-maps.yandex.ru/1.x/?l=map"

  private val backend = HttpURLConnectionBackend()

  private def getBounds(locations: List[Location]): String = {
    val N = locations.length
    val latAvg = locations.map(_.getLat).sum / N

    //    https://carto.com/blog/center-of-points/
    val lonAvg = locations.map { loc =>
      (math.sin(math.Pi * loc.getLon / 180), math.cos(math.Pi * loc.getLon / 180))
    }.foldLeft((0d, 0d)) {
      case (acc, (sin, cos)) =>
        (acc._1 + sin, acc._2 + cos)
    } match {
      case (sin, cos) => 180 * math.atan2(sin / N, cos / N) / math.Pi
    }

    val maxLatDiff = locations.map { loc =>
      math.abs(loc.getLat - latAvg)
    }.max

    val maxLonDiff = locations.map { loc =>
      math.abs(loc.getLon - lonAvg)
    }.max

    s"ll=$lonAvg,$latAvg&spn=${maxLonDiff * 2},${maxLatDiff * 3}"
  }

  private def getLine(locations: List[Location]): String = {
    val line = locations.sortBy(_.getTimestamp).map { loc =>
      s"${loc.getLon},${loc.getLat}"
    }.mkString(",")
    s"pl=$line"
  }

  private def getMarkers(locations: List[Location]): String = {
    val head = locations.head
    val tail = locations.last
    s"pt=${head.getLon},${head.getLat},pmgns~${tail.getLon},${tail.getLat},pmrds"
  }

  private def getHistory(driverIs: Long, from: DateTime, to: DateTime): Future[List[Location]] = {
    locationService.getHistory(driverIs, from, to).map { seq =>
      seq.toList
    }
  }

  def createMap(driverId: Long, from: DateTime, to: DateTime, width: Int = 500, heights: Int = 300): Future[String] = {
    getHistory(driverId, from, to).map { locations =>
      val boouds = getBounds(locations)
      val line = getLine(locations)
      val markers = getMarkers(locations)

      val url = s"$baseUrl&$boouds&$line&$markers&size=$width,$heights"

      val mapFile = new File(s"mapfile$driverId.png")

      val request = basicRequest.get(uri"$url")
        .response(asFile(mapFile))

      val response = request.send(backend)

      mapFile.getName
    }
  }

}
