package dao

import java.time.Instant

import dao.mapping.Location
import dao.repo.LocationRepo
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import implicits.DateTimeTransform._
import implicits.LocationTransform._
import misc.CompletionMarker
import org.joda.time.DateTime
import play.api.Logging

@Singleton
class LocationDao @Inject()(repo: LocationRepo)(implicit ec: ExecutionContext)
  extends BaseDao[Location, Long, LocationRepo](repo)(ec)
    with Logging {

  override def ensureExists(): Future[CompletionMarker] = {
    super.ensureExists().flatMap { _ =>
      //PUT some startup debug here
      Future.successful(CompletionMarker)
    }
  }

  def create(lat: Double, lon: Double, userId: Long): Future[common.Location] = {
    logger.info(s"Adding point $lat:$lon for user id $userId")
    create(Location(None, lat, lon, userId, DateTime.now()))
  }

  def create(location: common.Location): Future[common.Location] =
    repo.create(location).map(locationDbToModel)


  def bulkCreate(locations: List[common.Location]): Future[Option[Int]] =
    repo.bulkCreate(locations.map(locationModeltoDb))

  def getRange(userId: Long, from: DateTime, to: DateTime): Future[Seq[common.Location]] =
    repo.getRange(userId, from, to).map { seq =>
      seq.map(locationDbToModel)
    }

}
