package paulymorph.mock.configuration.route

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import akka.stream.scaladsl.{Flow, Source}
import paulymorph.mock.configuration._
import paulymorph.mock.configuration.stub._
import paulymorph.utils.Directives

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

  implicit lazy val stubConfigRoutable: Routable[StubConfiguration] = configuration => {
    import akka.http.scaladsl.server.Directives._
    configuration.stubs.map(_.toRoute).fold(reject)(_ ~ _)
  }

  implicit lazy val responseRoutable: Routable[Response] = {
    case response: SseEventsResponse =>
      import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._

      import scala.concurrent.duration._

      complete {
        DelayedSource.createMessageLike(response.events)
          .map(_.toSse)
          .takeWithin(response.timeout.getOrElse(10.minutes))
      }
    case response: WebSocketEventsResponse =>
      import scala.concurrent.duration._
      val source = DelayedSource.createMessageLike(response.events)
          .map(_.toWs)
      handleWebSocketMessages(Flow[Message].mapConcat(_ => Nil).merge(source).takeWithin(response.timeout.getOrElse(10.minutes)))
  }

  implicit lazy val responseStubRoutable: Routable[ResponseStub] = (stub: ResponseStub) => {
    val predicateDirective = stub.predicates.foldLeft(pass) { case (accDirective, predicate) =>
      accDirective & predicate.toDirective(Directable.predicateDirectable)
    }

    predicateDirective {
        Directives.cyclic(stub.responses.map(_.toRoute))
    }
  }

  implicit lazy val stubRoutable: Routable[Stub] = {
    case stub: ResponseStub => stub.toRoute
  }

  implicit lazy val mockRoutable: Routable[MockConfiguration] = {
    case mock: StubConfiguration => mock.toRoute
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
}

object SourcableSyntax {

  implicit class SourcableOps[T](value: T) {
    def toSource[E](implicit sourcable: Sourcable[T, E]) = sourcable.toSource(value)
  }

}