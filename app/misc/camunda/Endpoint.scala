package misc.camunda

import play.api.{Configuration, Logging}
import sttp.client3.{HttpURLConnectionBackend, NoBody}
import sttp.client3.quick.{basicRequest, _}
import sttp.model.Uri

case class Credentials(username: String, password: String)

sealed abstract class CamundaResult

case class Success(result: String) extends CamundaResult

object Fail extends CamundaResult


abstract class Endpoint(config: Configuration) extends Logging {

  def host: String = config.get[String]("camunda.address")

  def port: String = config.get[String]("camunda.port")

  implicit def defaultCreds: Credentials =
    Credentials(
      config.get[String]("camunda.user"),
      config.get[String]("camunda.pass"))

  val restEndpoint = "engine-rest"

  def resourceUrl: String

  def baseUrl = s"http://$host:$port/$restEndpoint/$resourceUrl"

  private val backend = HttpURLConnectionBackend()

  def get(urlPart: String, params: Map[String, String] = Map())(implicit creds: Credentials): CamundaResult = {

    val paramString = params.foldLeft("?") {
      case (acc, (k, v)) =>
        acc.concat(s"$k=$v")
    }
    val s = if (urlPart.isEmpty) s"$baseUrl$paramString" else s"$baseUrl/$urlPart$paramString"

    val url = uri"$s"

    val request = basicRequest
      .get(url)
      .body("")
      .contentType("application/json")
      .auth.basic(creds.username, creds.password)

    logger.info(s"Requesting $url")

    val response = request.send(backend)

    logger.info(s"Got response: ${response.code}: ${response.body}")

    response.code.code match {
      case 200 => Success(response.body.getOrElse(""))
      case _ => Fail
    }
  }

  def post(urlPart: String, body: String)(implicit creds: Credentials): CamundaResult = {
    val s = s"$baseUrl/$urlPart"
    val url = uri"$s"

    val request = basicRequest
      .post(url)
      .contentType("application/json")
      .body(body)
      .auth.basic(creds.username, creds.password)

    logger.info(s"Requesting $url with $body")

    val response = request.send(backend)

    logger.info(s"Got response: ${response.code}: ${response.body}")

    response.code.code match {
      case 204 => Success(response.body.getOrElse(""))
      case 200 => Success(response.body.getOrElse(""))
      case _ => Fail
    }
  }

  def put(urlPart: String, body: String)(implicit creds: Credentials): CamundaResult = {
    val s = s"$baseUrl/$urlPart"
    val url = uri"$s"

    val request = basicRequest
      .put(url)
      .contentType("application/json")
      .body(body)
      .auth.basic(creds.username, creds.password)

    logger.info(s"Requesting $url with $body")

    val response = request.send(backend)

    logger.info(s"Got response: ${response.code}: ${response.body}")

    response.code.code match {
      case 204 => Success(response.body.getOrElse(""))
      case _ => Fail
    }
  }
}
