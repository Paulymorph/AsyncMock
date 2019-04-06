package paulymorph.mock.configuration.sse

import akka.http.scaladsl.model.sse.ServerSentEvent

sealed trait Stub

case class SseStub(predicate: Predicate, response: Response) extends Stub

sealed trait Response

case class SequenceResponse(events: Seq[ServerSentEvent]) extends Response