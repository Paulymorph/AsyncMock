package paulymorph.mock.manager

import java.util.concurrent.atomic.AtomicReference

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.scalalogging.Logger
import paulymorph.mock.configuration.MockConfiguration
import paulymorph.utils.Directives

import scala.concurrent.Future

case class AdminMockConfigurationManager(adminPort: Int, endpointManager: MockEndpointManager)
                                        (implicit actorSystem: ActorSystem,
                                         materializer: Materializer) {
  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
  import paulymorph.mock.configuration.JsonUtils._

  private implicit val executionContext = actorSystem.dispatcher

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
    }
  }

  val swaggerRoute: Route = path("swagger") {
    getFromResource("swagger/index.html")
  } ~ getFromResourceDirectory("swagger")

  private val atomicBinding = new AtomicReference[Option[ServerBinding]](None)

  def start: Future[Unit] =
    Http().bindAndHandle(handler = logDirective(adminRoute ~ swaggerRoute), port = adminPort, interface = "0.0.0.0")
      .map(binding => atomicBinding.set(Some(binding)))
      .map(_ => ())

  private val logger = Logger[AdminMockConfigurationManager]

  private val logDirective = Directives.logRequestResponse(logger)


  def stop: Future[Unit] = for {
    _ <- endpointManager.deleteMocks
    _ <- atomicBinding.get().fold(Future.successful[Done](Done)) { binding =>
      binding.unbind()
    }
  } yield ()
}
