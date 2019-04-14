package paulymorph.mock.configuration.stub

import akka.http.scaladsl.model.sse.ServerSentEvent
import io.circe.Json

sealed trait Stub

case class ResponseStub(predicates: Seq[Predicate], response: Response) extends Stub

sealed trait Response

case class SseEventsResponse(events: Seq[ServerSentEvent]) extends Response

case class WebSocketEventsResponse(events: Seq[Json]) extends Response