package dao.implicits

import dao.mapping.Location
import IdTransform._
import scala.language.implicitConversions

object LocationTransform {

  implicit def locationDbToModel(location:Location):common.Location = {
     new common.Location(location.id, location.lat, location.lon, location.userId, location.timestamp)
  }

  implicit def locationModeltoDb(location:common.Location):Location = {
    Location(location.getId, location.getLat, location.getLon, location.getUserId, location.getTimestamp)
  }
}
