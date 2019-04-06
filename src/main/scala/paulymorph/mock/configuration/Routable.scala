package paulymorph.mock.configuration

import akka.NotUsed
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.{Directive0, Route}
import akka.stream.scaladsl.Source
import paulymorph.mock.configuration.sse._

trait Routable[T] {
  def toRoute(value: T): Route
}

object RoutableSyntax {

  implicit class RoutableOps[T](value: T) {
    def toRoute(implicit routable: Routable[T]) = routable.toRoute(value)
  }

}

object Routable {

  import DirectableSyntax.DirectableOps
  import RoutableSyntax.RoutableOps
  import SourcableSyntax.SourcableOps

  implicit lazy val sseConfigRoutable: Routable[SseConfiguration] = configuration => {
    import akka.http.scaladsl.server.Directives._
    configuration.stubs.map(_.toRoute).fold(reject)(_ ~ _)
  }

  implicit lazy val sseStubRoutable: Routable[SseStub] = (stub: SseStub) => {
    import Sourcable.reponseSourcable
    import Directable.predicateDirectable
    import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
    import akka.http.scaladsl.server.Directives._
    import scala.concurrent.duration._

    stub.predicate.toDirective(implicitly) {
      complete {
        stub.response.toSource[ServerSentEvent]
          .takeWithin(5 seconds)
      }
    }
  }

  implicit lazy val stubRoutable: Routable[Stub] = {
    case stub: SseStub => stub.toRoute
  }

  implicit lazy val mockRoutable: Routable[MockConfiguration] = {
    case mock: SseConfiguration => mock.toRoute
  }
}

trait Directable[T] {
  def toDirective(value: T): Directive0
}

object Directable {
  import akka.http.scaladsl.server.Directives._
  implicit lazy val predicateDirectable: Directable[Predicate] = {
    case All => pass
  }
}

object DirectableSyntax {

  implicit class DirectableOps[T](value: T) {
    def toDirective(implicit directable: Directable[T]) = directable.toDirective(value)
  }

}

trait Sourcable[T, E] {
  def toSource(value: T): Source[E, NotUsed]
}

object Sourcable {
  implicit lazy val reponseSourcable: Sourcable[Response, ServerSentEvent] = {
    case SequenceResponse(events) =>
      Source(events.toVector)
  }
}

object SourcableSyntax {

  implicit class SourcableOps[T](value: T) {
    def toSource[E](implicit sourcable: Sourcable[T, E]) = sourcable.toSource(value)
  }

}