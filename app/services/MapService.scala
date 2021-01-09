package services

import java.io.File

import common.{Location, Point, RoutePoint}
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

  private def getBounds(locations: List[Location], points: List[common.Point]): String = {
    val coords = locations.map { loc =>
      (loc.getLon, loc.getLat)
    } :::
      points.map { point =>
        (point.getLon, point.getLat)
      }

    val N = coords.length

    val latAvg = coords.map {
      case (_, lat: Double) => lat
    }.sum / N

    //    https://carto.com/blog/center-of-points/
    val lonAvg = coords.map {
      case (lon: Double, _) =>
        (math.sin(math.Pi * lon / 180), math.cos(math.Pi * lon / 180))
    }.foldLeft((0d, 0d)) {
      case (acc, (sin, cos)) =>
        (acc._1 + sin, acc._2 + cos)
    } match {
      case (sin, cos) => 180 * math.atan2(sin / N, cos / N) / math.Pi
    }

    val maxLatDiff = coords.map {
      case (_, lat: Double) =>
        math.abs(lat - latAvg)
    }.max

    val maxLonDiff = coords.map {
      case (lon: Double, _) =>
        math.abs(lon - lonAvg)
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
    if (locations.isEmpty) {
      return ""
    }
    val head = locations.head
    val tail = locations.last
    s"pt=${head.getLon},${head.getLat},pmgns~${tail.getLon},${tail.getLat},pmrds"
  }

  private def getHistory(driverIs: Long, from: DateTime, to: DateTime): Future[List[Location]] =
    locationService.getHistory(driverIs, from, to).map { seq =>
      seq.toList
    }

  def getPoints(points: List[RoutePoint]): String = {
    points.zipWithIndex.map {
      case (point, i) =>
        val color = if (point.getVisited) "gn" else "rd"
        s"${point.getPoint.getLon},${point.getPoint.getLat},pm2${color}m${i + 1}"
    }.mkString("~")
  }

  def createMap(driverId: Long, from: DateTime, to: DateTime,
                width: Int = 500, heights: Int = 300,
                points: List[RoutePoint] = List.empty): Future[String] = {
    getHistory(driverId, from, to).map { locations =>
      val boouds = getBounds(locations, points.map(_.getPoint))
      val line = getLine(locations)
      val markers = getMarkers(locations)

      val url =
        if (points.isEmpty) {
          s"$baseUrl&$boouds&$line&$markers&size=$width,$heights"
        } else {
          val pointString = getPoints(points)
          if(markers.isBlank){
            s"$baseUrl&$boouds&$line&pt=$pointString&size=$width,$heights"
          } else{
            s"$baseUrl&$boouds&$line&$markers~$pointString&size=$width,$heights"
          }
        }

      val mapFile = new File(s"mapfile-$driverId-${from.toString("DD-MM-YYYY")}.png")

      val request = basicRequest.get(uri"$url")
        .response(asFile(mapFile))

      val response = request.send(backend)

      mapFile.getName
    }
  }

}
