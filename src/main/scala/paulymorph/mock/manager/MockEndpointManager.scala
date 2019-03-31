package paulymorph.mock.manager

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import paulymorph.mock.configuration._

import scala.concurrent.Future
import scala.util.Random

trait MockEndpointManager {
  def addMock(mock: MockConfiguration): Future[Unit]
  def deleteMock(port: Int): Future[MockConfiguration]
}

case class AdminMockConfigurationManager(adminPort: Int)
                                        (implicit actorSystem: ActorSystem, materializer: Materializer) extends MockEndpointManager {
  import akka.http.scaladsl.server.Directives._

  implicit val executionContext = actorSystem.dispatcher

  val adminRoute: Route = path("mock") {
    (get & parameter('port.as[Int].?)) { portOpt =>
      val port = portOpt.getOrElse(Random.nextInt(1000) + 8000)
      val sampleMock = SimpleMockConfiguration(port, Seq(Stub(AllPredicate, SimpleResponse(Random.nextString(20)))))
      onSuccess(addMock(sampleMock)) {
        complete(StatusCodes.Created, s"Created a mock on port $port")
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
