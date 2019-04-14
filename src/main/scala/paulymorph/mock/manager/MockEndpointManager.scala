package paulymorph.mock.manager

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import paulymorph.mock.configuration.{MockConfiguration, Routable}
import paulymorph.mock.configuration.stub.{ResponseStub, StubConfiguration}

import scala.concurrent.Future

trait MockEndpointManager {
  def addMock(mock: MockConfiguration): Future[Unit]
  def deleteMock(port: Int): Future[MockConfiguration]
}

case class AdminMockConfigurationManager(adminPort: Int)
                                        (implicit actorSystem: ActorSystem,
                                         materializer: Materializer) extends MockEndpointManager {
  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
  import io.circe.generic.auto._
  import paulymorph.mock.configuration.RoutableSyntax.RoutableOps

  implicit val executionContext = actorSystem.dispatcher

  val adminRoute: Route = path("mock") {
    (post & entity(as[StubConfiguration])) { stub =>
      onSuccess(addMock(stub)) {
        complete(StatusCodes.Created, s"Created a mock on port ${stub.port}")
      }
    } ~
    get {
      import paulymorph.mock.configuration.stub.{WebSocketEventsResponse, SseEventsResponse}
      import io.circe.generic.auto._, io.circe.syntax._
      import io.circe.parser.parse
      val sseStub = ResponseStub(Seq.empty, SseEventsResponse(Seq(ServerSentEvent("sse"))))
      val wsStub = ResponseStub(Seq.empty, WebSocketEventsResponse(Seq(parse("404").right.get)))
      complete {
        Seq(StubConfiguration(123, Seq(sseStub)).asJson,
          StubConfiguration(123, Seq(wsStub)).asJson
        )
      }
    }
  }

  override def addMock(mock: MockConfiguration): Future[Unit] =
    Http().bindAndHandle(handler = mock.toRoute(Routable.mockRoutable), port = mock.port, interface = "localhost")
    .map(_ => ())

  override def deleteMock(port: Int): Future[MockConfiguration] = Future.failed(???)

  def start: Future[Unit] =
    Http().bindAndHandle(handler = adminRoute, port = adminPort, interface = "localhost")
      .map(_ => ())

  def stop: Future[Unit] = Future.failed(???)
}