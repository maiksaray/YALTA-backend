package dao.repo

import java.sql.Timestamp

import com.byteslounge.slickrepo.meta.Keyed
import dao.mapping.Location
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.BaseTypedType

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LocationRepo @Inject()(override val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends CommonRepo[Location, Long](dbConfigProvider) {

  import dbConfig._
  import profile.api._

  override type TableType = Locations

  override def pkType = implicitly[BaseTypedType[Long]]

  override def tableQuery = TableQuery[Locations]

  class Locations(tag: Tag) extends Table[Location](tag, "locations") with Keyed[Long] {
    override def id = column[Long]("id", O.AutoInc)

    def lat = column[Double]("lat")

    def lon = column[Double]("lon")

    def userId = column[Long]("userId")

    def timestamp = column[Timestamp]("timestamp", O.SqlType("timestamp default now()"))

    //    TODO:add FK for userID

    override def * = (id.?, lat, lon, userId, timestamp) <> ((Location.apply _).tupled, Location.unapply)
  }

//  This is commented since Autoinc doesn't work for timestamp in DB,
//  So we use default save DBIO and pre-generated timestamp
//  override def save(location: Location)(implicit ec: ExecutionContext): DBIO[Location] = {
//    (tableQuery returning tableQuery.map(_.timestamp) += location)
//      .map(location.withTimestamp)
//  }

  override def create(location: Location): Future[Location] = db.run {
//  This is commented since Autoinc doesn't work for timestamp in DB,
//  So we use default save DBIO and pre-generated timestamp
//    save(location)
    save(location.withTimestamp(new Timestamp(System.currentTimeMillis())))
  }
}
