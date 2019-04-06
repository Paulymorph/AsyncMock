package paulymorph.mock.manager

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import paulymorph.mock.configuration._
import paulymorph.mock.configuration.sse.SseConfiguration

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
  import RoutableSyntax.RoutableOps
  import Routable.mockRoutable

  implicit val executionContext = actorSystem.dispatcher

  val adminRoute: Route = path("mock") {
    (post & entity(as[SseConfiguration])) { sseConfiguration =>
      onSuccess(addMock(sseConfiguration)) {
        complete(StatusCodes.Created, s"Created a mock on port ${sseConfiguration.port}")
      }
    }
  }

  override def addMock(mock: MockConfiguration): Future[Unit] =
    Http().bindAndHandle(handler = mock.toRoute, port = mock.port, interface = "localhost")
    .map(_ => ())

  override def deleteMock(port: Int): Future[MockConfiguration] = Future.failed(???)

  def start: Future[Unit] =
    Http().bindAndHandle(handler = adminRoute, port = adminPort, interface = "localhost")
      .map(_ => ())

  def stop: Future[Unit] = Future.failed(???)
}
