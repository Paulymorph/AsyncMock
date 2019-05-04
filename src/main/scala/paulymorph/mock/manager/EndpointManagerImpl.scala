package paulymorph.mock.manager

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.Materializer
import com.typesafe.scalalogging.Logger
import paulymorph.mock.configuration.MockConfiguration
import paulymorph.mock.configuration.route.Routable
import paulymorph.utils.Directives

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Success

class EndpointManagerImpl(implicit actorSystem: ActorSystem, materializer: Materializer) extends MockEndpointManager {
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  import paulymorph.mock.configuration.route.RoutableSyntax.RoutableOps

  private val portBindings = TrieMap.empty[Int, (ServerBinding, MockConfiguration)]

  override def addMock(mock: MockConfiguration): Future[Unit] = {
    if (portBindings.contains(mock.port))
      Future.failed(PortAlreadyInUse(mock.port))
    else {
      val route = mock.toRoute(Routable.mockRoutable)
      for {
        binding <- Http().bindAndHandle(handler = loggerDirective(route), port = mock.port, interface = "0.0.0.0")
        _ = portBindings += mock.port -> (binding, mock)
        _ = logger.info(s"Successfully bound on port ${mock.port}")
      } yield ()
    }
  }

  override def deleteMock(port: Int): Future[MockConfiguration] = {
    for {
      bindingOpt <- Future.successful(portBindings.remove(port))
      (binding, mock) = bindingOpt.getOrElse(throw NoEndpointStartedOnPort(port))
      _ <- binding.unbind()
      _ = logger.info(s"Endpoint on $port successfully stopped")
    } yield mock
  }

  private lazy val logger = Logger[EndpointManagerImpl]

  private lazy val loggerDirective = Directives.logRequestResponse(logger)

  override def getMock(port: Int): Future[MockConfiguration] = for {
    bindingOpt <- Future.successful(portBindings.get(port))
    (_, mock) = bindingOpt.getOrElse(throw NoEndpointStartedOnPort(port))
  } yield mock

  override def deleteMocks: Future[Seq[MockConfiguration]] = {
    val bindings = portBindings.values.toSeq
    val unbindingsFuture = bindings.map { case (binding, configuration) =>
      binding.unbind()
        .map(_ => configuration)
    }

    Future.sequence(unbindingsFuture)
      .andThen { case Success(_) => portBindings.clear() }
  }
}
