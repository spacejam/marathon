package mesosphere.marathon.event.http

import akka.actor._
import akka.pattern.ask
import mesosphere.marathon.event._
import mesosphere.marathon.event.http.SubscribersKeeperActor.GetSubscribers
import play.api.libs.json.{ JsValue, Json }
import spray.client.pipelining.{ sendReceive, _ }
import spray.http.{ HttpRequest, HttpResponse }
import spray.httpx.PlayJsonSupport
import mesosphere.marathon.api.v2.json.Formats._

import scala.concurrent.Future
import scala.util.{ Failure, Success }

class HttpEventActor(val subscribersKeeper: ActorRef) extends Actor with ActorLogging with PlayJsonSupport {

  implicit val ec = HttpEventModule.executionContext
  implicit val timeout = HttpEventModule.timeout

  val pipeline: HttpRequest => Future[HttpResponse] = (
    addHeader("Accept", "application/json")
    ~> sendReceive)

  def receive: Receive = {
    case event: MarathonEvent =>
      broadcast(event)
    case _ =>
      log.warning("Message not understood!")
  }

  def broadcast(event: MarathonEvent): Unit = {
    log.info("POSTing to all endpoints.")
    (subscribersKeeper ? GetSubscribers).mapTo[EventSubscribers].foreach {
      _.urls.foreach { post(_, event) }
    }
  }

  def post(urlString: String, event: MarathonEvent): Unit = {
    log.info("Sending POST to:" + urlString)

    val request = Post(urlString, eventToJson(event))
    val response = pipeline(request)

    response.onComplete {
      case Success(res) =>
        if (res.status.isFailure)
          log.warning(s"Failed to post $event to $urlString")

      case Failure(t) =>
        log.warning(s"Failed to post $event to $urlString", t)
    }
  }
}

