package paulymorph.mock.manager

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import paulymorph.mock.configuration.StubConfiguration
import paulymorph.mock.configuration.stub._

import scala.concurrent.Future

case class AdminMockConfigurationManager(adminPort: Int, endpointManager: MockEndpointManager)
                                        (implicit actorSystem: ActorSystem,
                                         materializer: Materializer) {
  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
  import paulymorph.mock.configuration.JsonUtils._

  implicit val executionContext = actorSystem.dispatcher

  val adminRoute: Route = pathPrefix("mock") {
    (post & entity(as[StubConfiguration])) { stub =>
      onSuccess(endpointManager.addMock(stub)) {
        complete(StatusCodes.Created, s"Created a mock on port ${stub.port}")
      }
    } ~
    pathPrefix(IntNumber) { port =>
      get {
        complete(endpointManager.getMock(port))
      } ~
      delete {
        complete(endpointManager.deleteMock(port))
      }
    } ~
    get {
      import io.circe.parser.parse
      import io.circe.syntax._
      import paulymorph.mock.configuration.stub.{SseEventsResponse, WebSocketEventsResponse}

      val sseStub = ResponseStub(Seq.empty, SseEventsResponse(Seq(ServerSentEvent("sse"))))
      val wsStub = ResponseStub(Seq.empty, WebSocketEventsResponse(Seq(parse("404").right.get)))
      complete {
        Seq(StubConfiguration(123, Seq(sseStub)).asJson,
          StubConfiguration(123, Seq(wsStub)).asJson
        )
      }
    }
  }

  val swaggerRoute: Route = path("swagger") {
    getFromResource("swagger/index.html")
  } ~ getFromResourceDirectory("swagger")

  def start: Future[Unit] =
    Http().bindAndHandle(handler = adminRoute ~ swaggerRoute, port = adminPort, interface = "localhost")
      .map(_ => ())

  def stop: Future[Unit] = Future.failed(???)
}
