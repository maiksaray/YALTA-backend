package dao.implicits

import scala.language.implicitConversions

object IdTransform {

  implicit def nullableToOption(id: java.lang.Long): Option[Long] = id match {
    case null => Some(id)
    case _ => None
  }

  implicit def OptionToNullableLong(id: Option[Long]): java.lang.Long = id match {
    case Some(v) => v
    case None => null
  }
}
