package dao

import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

import dao.mapping.Session
import javax.inject.Singleton
import play.api.Logging

import scala.concurrent.Future


trait SessionDao extends Logging {

  def getSession(token: String): Future[Option[Session]]

  def generateToken(username: String): Future[String]

  def deleteSession(token: String): Future[Option[Session]]
}

@Singleton
class InMemSessionDao extends SessionDao {

  private val sessions = new ConcurrentHashMap[String, Session]()

  override def getSession(token: String): Future[Option[Session]] =
    Future.successful(
      sessions.get(token) match {
        case null => None
        case session => Some(session)
      }
    )

  override def generateToken(username: String): Future[String] = {
    val token = s"$username-token-${UUID.randomUUID().toString}"
    sessions.put(token, Session(token, username, LocalDateTime.now().plusHours(6)))
    logger.info(s"Generated and saved session token for $username")
    Future.successful(token)
  }

  override def deleteSession(token: String): Future[Option[Session]] =
    Future.successful(
      sessions.remove(token) match {
        case null => None
        case session => Some(session)
      }
    )
}
