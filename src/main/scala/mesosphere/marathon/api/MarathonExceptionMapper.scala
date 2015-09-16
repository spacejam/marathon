package mesosphere.marathon.api

import javax.ws.rs.ext.{ Provider, ExceptionMapper }
import javax.ws.rs.core.{ MediaType, Response }
import com.fasterxml.jackson.databind.JsonMappingException

import scala.concurrent.TimeoutException
import mesosphere.marathon.{
  AppLockedException,
  BadRequestException,
  ConflictingChangeException,
  UnknownAppException
}
import mesosphere.marathon.state.Identifiable
import com.sun.jersey.api.NotFoundException
import com.fasterxml.jackson.core.JsonParseException
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response.Status
import org.apache.log4j.Logger
import play.api.libs.json.JsResultException

@Provider
class MarathonExceptionMapper extends ExceptionMapper[Exception] {

  private[this] val log = Logger.getLogger(getClass.getName)

  def toResponse(exception: Exception): Response = {
    // WebApplicationException are things like invalid requests etc, no need to log a stack trace
    if (!exception.isInstanceOf[WebApplicationException]) {
      log.warn("", exception)
    }

    Response
      .status(statusCode(exception))
      .entity(entity(exception))
      .`type`(MediaType.APPLICATION_JSON)
      .build
  }

  //scalastyle:off magic.number cyclomatic.complexity
  private def statusCode(exception: Exception): Int = exception match {
    //scalastyle:off magic.number
    case e: IllegalArgumentException   => 422 // Unprocessable entity
    case e: TimeoutException           => 504 // Gateway timeout
    case e: UnknownAppException        => 404 // Not found
    case e: AppLockedException         => 409 // Conflict
    case e: ConflictingChangeException => 409 // Conflict
    case e: BadRequestException        => 400 // Bad Request
    case e: JsonParseException         => 400 // Bad Request
    case e: JsResultException          => 400 // Bad Request
    case e: JsonMappingException       => 400 // Bad Request
    case e: WebApplicationException    => e.getResponse.getStatus
    case _                             => 500 // Internal server error
    //scalastyle:on
  }

  private def entity(exception: Exception): Any = exception match {
    case e: NotFoundException =>
      Map("message" -> s"URI not found: ${e.getNotFoundUri.getRawPath}")
    case e: AppLockedException =>
      Map(
        "message" -> e.getMessage,
        "deployments" -> e.deploymentIds.map(Identifiable)
      )
    case e: JsonParseException =>
      Map("message" -> e.getOriginalMessage)
    case e: JsResultException =>
      val errors = e.errors.map {
        case (path, errs) => Map("path" -> path.toString(), "errors" -> errs.map(_.message))
      }
      Map(
        "message" -> s"Invalid JSON",
        "details" -> errors
      )
    case e: WebApplicationException =>
      //scalastyle:off null
      if (e.getResponse.getEntity != null) {
        Map("message" -> e.getResponse.getEntity)
      }
      else if (Status.fromStatusCode(e.getResponse.getStatus) != null) {
        Map("message" -> Status.fromStatusCode(e.getResponse.getStatus).getReasonPhrase)
      }
      else {
        Map("message" -> e.getMessage)
      }
    case _ =>
      Map("message" -> exception.getMessage)
  }
}
