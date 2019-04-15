package paulymorph.mock.manager

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.directives.LoggingMagnet
import akka.http.scaladsl.server.{Route, RouteResult}
import akka.stream.Materializer
import com.typesafe.scalalogging.Logger
import paulymorph.mock.configuration.stub._
import paulymorph.mock.configuration.{MockConfiguration, StubConfiguration}

import scala.concurrent.Future

case class AdminMockConfigurationManager(adminPort: Int, endpointManager: MockEndpointManager)
                                        (implicit actorSystem: ActorSystem,
                                         materializer: Materializer) {
  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
  import paulymorph.mock.configuration.JsonUtils._

  implicit val executionContext = actorSystem.dispatcher

  val adminRoute: Route = pathPrefix("mock") {
    (post & entity(as[MockConfiguration])) { stub =>
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
    Http().bindAndHandle(handler = logDirective(adminRoute ~ swaggerRoute), port = adminPort, interface = "localhost")
      .map(_ => ())

  val logDirective = logRequestResult({
    def logRequestAndResponse(req: HttpRequest)(res: RouteResult): Unit = {
      logger.info(req.toString)
      logger.info(res.toString)
    }

    LoggingMagnet(_ => logRequestAndResponse)
  })

  private val logger = Logger[AdminMockConfigurationManager]


  def stop: Future[Unit] = Future.failed(???)
}
