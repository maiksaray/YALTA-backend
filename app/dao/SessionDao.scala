package dao

import java.time.LocalDateTime
import java.util.UUID

import dao.mapping.{Session, User}
import javax.inject.Singleton

import scala.collection.mutable
import implicits.UserTransform._


@Singleton
class SessionDao {

  private val sessions = mutable.Map.empty[String, Session]

  def getSession(token: String): Option[Session] = sessions.get(token)

  def generateToken(user:common.User):String = generateToken(user.name)

  def generateToken(username: String): String = {
    val token = s"$username-token-${UUID.randomUUID().toString}"
    sessions.put(token, Session(token, username, LocalDateTime.now().plusHours(6)))
    token
  }

  def deleteSession(token: String): Option[Session] = sessions.remove(token)
}
