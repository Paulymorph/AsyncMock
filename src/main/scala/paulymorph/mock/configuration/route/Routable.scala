package paulymorph.mock.configuration.route

import akka.NotUsed
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import akka.stream.scaladsl.Source
import paulymorph.mock.configuration._
import paulymorph.mock.configuration.stub._
import paulymorph.mock.configuration.stub.http._
import paulymorph.mock.configuration.stub.websocket.WsReaction
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
      handleWebSocketMessages(WsReaction.toFlow(response.reactions).merge(source).takeWithin(response.timeout.getOrElse(10.minutes)))
  }

  implicit lazy val responseStubRoutable: Routable[ResponseStub] = (stub: ResponseStub) => {
    val predicateDirective = stub.predicates.foldLeft(pass) { case (accDirective, predicate) =>
      accDirective & predicate.toDirective(Directable.predicateDirectable)
    }
    val responseRoute = Directives.cyclic(stub.responses.map(_.toRoute))

    predicateDirective {
      responseRoute
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

  import DirectableSyntax._
  import akka.http.scaladsl.server.Directives._

  implicit lazy val predicateDirectable: Directable[HttpPredicate] = {
    case And(predicates) => predicates.map(_.toDirective(predicateDirectable)).fold(pass)(_ & _)
    case Or(predicates) => predicates.map(_.toDirective(predicateDirectable)).fold(pass)(_ | _)

    case Equals(MethodExpectation(expectedMethod)) =>
      method(HttpMethods.getForKeyCaseInsensitive(expectedMethod).getOrElse(???))
    case Contains(MethodExpectation(expectedMethod)) =>
      method(HttpMethods.getForKeyCaseInsensitive(expectedMethod).getOrElse(???))
    case StartsWith(MethodExpectation(expectedMethod)) =>
      method(HttpMethods.getForKeyCaseInsensitive(expectedMethod).getOrElse(???))

    case Equals(PathExpectation(expectedPath)) =>
      path(expectedPath)
    case Equals(BodyExpectation(expectedBody)) => ???
    case Equals(QueryExpectation(expectedQuery)) =>
      parameterMap.flatMap { actualParams =>
        validate(expectedQuery.forall(e => actualParams.toSet.contains(e)), s"Query $actualParams did not equal $expectedQuery")
      }

    case Contains(PathExpectation(expectedPath)) =>
      extractUri.flatMap { uri =>
        val fullPath = uri.toRelative.path.dropChars(1).toString
        validate(fullPath.contains(expectedPath), s"Path $fullPath did not contain $expectedPath")
      }
    case Contains(BodyExpectation(expectedBody)) => ???
    case Contains(QueryExpectation(expectedQuery)) =>
      parameterMap.flatMap { actualParams =>
        validate(expectedQuery.forall {
          case (key, expectedSubstring) =>
            actualParams.get(key)
              .exists(_.contains(expectedSubstring))
        }, s"Query $actualParams did not contain $expectedQuery")
      }

    case StartsWith(BodyExpectation(expectedBody)) => ???
    case StartsWith(PathExpectation(expectedPath)) =>
      extractUri.flatMap { uri =>
        val fullPath = uri.toRelative.path.dropChars(1).toString
        validate(fullPath.startsWith(expectedPath), s"Path $fullPath did not match prefix $expectedPath")
      }
    case StartsWith(QueryExpectation(expectedQuery)) =>
      parameterMap.flatMap { actualParams =>
        validate(expectedQuery.forall {
          case (key, expectedPrefix) =>
            actualParams.get(key)
              .exists(_.startsWith(expectedPrefix))
        }, s"Query $actualParams did not startWith $expectedQuery")
      }
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

object SourcableSyntax {

  implicit class SourcableOps[T](value: T) {
    def toSource[E](implicit sourcable: Sourcable[T, E]) = sourcable.toSource(value)
  }

}