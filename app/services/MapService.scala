package services

import common.Location
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.Logging
import sttp.client3.HttpURLConnectionBackend

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MapService @Inject()(locationService: LocationService)(implicit ec: ExecutionContext) extends Logging {

  def baseUrl = s"https://static-maps.yandex.ru/1.x/?l=map&"

  private val backend = HttpURLConnectionBackend()

  private def getBounds(locations: List[Location]): String = {
    val latAvg = locations.map(_.getLat).sum / locations.length

  }

  private def getLine(locations: List[Location]): String = {

  }

  private def getMarkers(locations: List[Location]): String = {

  }

  private def getHistory(driverIs: Long, from: DateTime, to: DateTime): Future[List[Location]] = {
    locationService.getHistory(driverIs, from, to).map(
      seq => seq.toList
    )
  }

}
