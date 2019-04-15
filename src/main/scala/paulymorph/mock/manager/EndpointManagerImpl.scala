package paulymorph.mock.manager

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.Materializer
import com.typesafe.scalalogging.Logger
import paulymorph.mock.configuration.{Routable, StubConfiguration}

import scala.concurrent.{ExecutionContextExecutor, Future}

class EndpointManagerImpl(implicit actorSystem: ActorSystem, materializer: Materializer) extends MockEndpointManager {
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  import EndpointManagerImpl.portBindings
  import paulymorph.mock.configuration.RoutableSyntax.RoutableOps

  override def addMock(mock: StubConfiguration): Future[Unit] = {
    if (portBindings.contains(mock.port))
      Future.failed(PortAlreadyInUse(mock.port))
    else {
      for {
        binding <- Http().bindAndHandle(handler = mock.toRoute(Routable.stubConfigRoutable), port = mock.port, interface = "localhost")
        _ = portBindings += mock.port -> (binding, mock)
        _ = logger.info(s"Successfully bound on port ${mock.port}")
      } yield Right()
    }
  }

  def deleteMock(port: Int): Future[StubConfiguration] = {
    for {
      bindingOpt <- Future.successful(portBindings.remove(port))
      (binding, mock) = bindingOpt.getOrElse(throw NoEndpointStartedOnPort(port))
      _ <- binding.unbind()
      _ = logger.info(s"Endpoint on $port successfully stopped")
    } yield mock
  }

  private def logger = Logger[EndpointManagerImpl]

  override def getMock(port: Int): Future[StubConfiguration] = for {
    bindingOpt <- Future.successful(portBindings.get(port))
    (_, mock) = bindingOpt.getOrElse(throw NoEndpointStartedOnPort(port))
  } yield mock
}

object EndpointManagerImpl {
  import scala.collection.concurrent.TrieMap

  private val portBindings = TrieMap.empty[Int, (ServerBinding, StubConfiguration)]
}