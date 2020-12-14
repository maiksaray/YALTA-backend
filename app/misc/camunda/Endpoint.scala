package misc.camunda

import play.api.Logging
import sttp.client3.HttpURLConnectionBackend
import sttp.client3.quick.{basicRequest, _}

abstract class Endpoint extends Logging {

  //    TODO: move to application.conf
  val camundaUrl = "http://localhost:8080/"
  val restEndpoint = "engine-rest/"
  val username = "yalta"
  val password = "yalta"

  val backend = HttpURLConnectionBackend()

  def resourceUrl: String

  def baseUrl = camundaUrl + restEndpoint + resourceUrl

  def post(urlPart: String, body: String) = {
    val url = uri"$baseUrl/$urlPart"

    val request = basicRequest
      .post(url)
      .contentType("application/json")
      .body(body)
      .auth.basic(username, password)

    logger.info(s"Requesting $url with $body")

    val response = request.send(backend)

    logger.info(s"Got response: ${response.statusText}: ${response.body}")

    response
  }
}
