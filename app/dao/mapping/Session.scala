package dao.mapping

import java.time.LocalDateTime

case class Session(token: String, username: String, expiration: LocalDateTime)
