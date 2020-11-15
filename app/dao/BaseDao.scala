package dao

import com.byteslounge.slickrepo.meta.Entity
import dao.repo.CommonRepo

import scala.concurrent.{ExecutionContext, Future}

class BaseDao[T <: Entity[T, ID], ID, RT <: CommonRepo[T, ID]](repo: RT)(implicit ec: ExecutionContext) {
  def ensureExists(): Future[Unit] =
    repo.createTable()
}
