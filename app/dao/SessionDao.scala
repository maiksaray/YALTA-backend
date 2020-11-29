package dao

import java.time.LocalDateTime
import java.util.UUID

import dao.implicits.UserTransform._
import dao.mapping.Session
import javax.inject.Singleton
import play.api.Logging

import scala.collection.mutable


@Singleton
class SessionDao extends Logging{

  private val sessions = mutable.Map.empty[String, Session]

  def getSession(token: String): Option[Session] = sessions.get(token)

  def generateToken(user:common.User):String = generateToken(user.name)

  def generateToken(username: String): String = {
    val token = s"$username-token-${UUID.randomUUID().toString}"
    sessions.put(token, Session(token, username, LocalDateTime.now().plusHours(6)))
    logger.info(s"Generated and saved session token for $username")
    token
  }
}
