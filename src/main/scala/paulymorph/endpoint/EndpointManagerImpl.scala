package paulymorph.endpoint

import java.net.BindException

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContextExecutor, Future}

class EndpointManagerImpl(implicit actorSystem: ActorSystem, materializer: Materializer) extends EndpointManager {
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  import EndpointManagerImpl.portBindings
  import paulymorph.utils.FutureUtils

  override def startEndpoint(port: Int, route: Route): Future[Either[PortAlreadyInUse, Unit]] = {
    if (portBindings.contains(port))
      Future.successful(Left(PortAlreadyInUse(port)))
    else {
      for {
        binding <- Http().bindAndHandle(route, "localhost", port)
        _ = portBindings += port -> binding
        _ = logger.info(s"Successfully bound on port $port")
      } yield Right()
    }
  }

  override def stopEndpoint(port: Int): Future[Either[NoEndpointStartedOnPort, Unit]] = {
    for {
      bindingOpt <- Future.successful(portBindings.remove(port))
      unbindingOpt <- FutureUtils.optTraverse(bindingOpt) { binding =>
        binding.unbind()
          .map { _ =>
            logger.info(s"Endpoint on $port successfully stopped")
            Right(()): Either[NoEndpointStartedOnPort, Unit]
          }
      }
      result = unbindingOpt.getOrElse(Left(NoEndpointStartedOnPort(port)))
    } yield result
  }

  override def replaceEndpoint(port: Int, newRoute: Route): Future[Either[NoEndpointStartedOnPort, Unit]] =
    for {
      stopResult <- stopEndpoint(port)
      _ <- FutureUtils
        .eitherTraverse(stopResult)(_ => startEndpoint(port, newRoute))
          .filter(_.right.exists(_.isRight))
    } yield stopResult

  override def startOrReplaceEndpoint(port: Int, route: Route): Future[Unit] = {
    replaceEndpoint(port, route).flatMap {
        case Right(success) => Future.successful(())
        case Left(nothingStarted) => startEndpoint(port, route).filter(_.isRight).map(_ => ())
    }
  }

  private def logger = Logger[EndpointManagerImpl]
}

object EndpointManagerImpl {

  import scala.collection.concurrent.TrieMap

  private val portBindings = TrieMap.empty[Int, ServerBinding]
}
