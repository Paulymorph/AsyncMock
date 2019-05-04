package paulymorph.mock.configuration.stub

import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.ws.TextMessage
import io.circe.Json
import paulymorph.mock.configuration.stub.http.HttpPredicate
import paulymorph.mock.configuration.stub.websocket.WsReaction

import scala.concurrent.duration.FiniteDuration

sealed trait Stub

case class ResponseStub(predicates: Seq[HttpPredicate], responses: Seq[Response]) extends Stub

sealed trait Response

trait MessageLike {
  def data: Json
  def delay: Option[FiniteDuration]
}

case class SseMessage(data: Json,
                      delay: Option[FiniteDuration],
                      eventType: Option[String] = None,
                      id:        Option[String] = None,
                      retry:     Option[Int]    = None) extends MessageLike {
  def toSse: ServerSentEvent =
    ServerSentEvent(data.toString, eventType = eventType, id = id, retry = retry)
}
case class WsMessage(data: Json, delay: Option[FiniteDuration]) extends MessageLike {
  def toWs: akka.http.scaladsl.model.ws.Message = TextMessage(data.toString)
}

sealed trait EventsSequenceLikeResponse extends Response {
  def events: Seq[MessageLike]
  def timeout: Option[FiniteDuration]
}

case class SseEventsResponse(events: Seq[SseMessage], timeout: Option[FiniteDuration]) extends EventsSequenceLikeResponse

case class WebSocketEventsResponse(events: Seq[WsMessage], reactions: Seq[WsReaction], timeout: Option[FiniteDuration]) extends EventsSequenceLikeResponse